import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class cliente {

    private int PUERTO = 1234; //Puerto para la conexión
    private String HOST = "localhost"; //Host para la conexión
    private int PUERTO_MULTICAST = 1234; //Puerto para multicast
    private String IP_MULTICAST = "0.0.0.0"; //IP para multicast

    protected String mensajeServidor; //Mensajes entrantes (recibidos) en el servidor
    protected ServerSocket ss; //Socket del servidor
    protected Socket cs; //Socket del cliente
    public void startClient() //Método para iniciar el cliente
    {
        try {
            Scanner scan = new Scanner(System.in);
            System.out.print("Ingrese IP Servidor Central: ");
            HOST = scan.nextLine();
            System.out.print("Ingrese puerto Servidor Central: ");
            PUERTO = Integer.parseInt(scan.nextLine());
            cs = new Socket(HOST, PUERTO); //Socket para el cliente en localhost en puerto 1234
            while (true) {
                System.out.println("Ingrese Opción:");
                System.out.println("1-Enviar Mensaje.");
                System.out.println("2-Pointless.");
                System.out.println("3-Cerrar Conexión.");
                System.out.println("4-Escuchar Multicast");
                //Flujo de datos hacia el servidor
                //System.out.println("Ingrese Mensaje");

                String text = scan.nextLine();
                if(text.equals("1")/**1**/) {
                    DataOutputStream salidaServidor = new DataOutputStream(cs.getOutputStream());
                    DataInputStream entrada = new DataInputStream(cs.getInputStream());

                    //Se enviarán dos mensajes
                    for (int i = 0; i < 1; i++) {
                        System.out.println("Ingrese Mensaje");
                        Scanner scan1 = new Scanner(System.in);
                        String text1 = scan.nextLine();
                        //Se escribe en el servidor usando su flujo de datos
                        salidaServidor.writeUTF(text1);
                    }
                    //Esperamos el mensaje del servidor
                    mensajeServidor = entrada.readUTF();
                    System.out.println(mensajeServidor);
                }
                else if(text.equals("3")/**3**/) {
                    cs.close();//Fin de la conexión
                    break;
                }
                else if(text.equals("4")) {
                    System.out.println("IP Multicast: ");
                    IP_MULTICAST = scan.nextLine();
                    System.out.println("Puerto Multicast: ");
                    PUERTO_MULTICAST = Integer.parseInt(scan.nextLine());

                    MultithreadListen ml = new MultithreadListen(IP_MULTICAST, PUERTO_MULTICAST, 0);
                    ml.start();
                }
                else{
                    System.out.println("¯\\_(ツ)_/¯");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}

class MultithreadListen extends Thread {
    private String IP_multicast;
    private int puerto_multicast;
    private int clientID = -1;

    MultithreadListen (String ip, int puerto, int i) {
        IP_multicast = ip;
        puerto_multicast = puerto;
        clientID = i;
    }

    public void run() {
        try {
            MulticastSocket ms = new MulticastSocket(puerto_multicast);

            //Código para elegir la interfaz de red por la que realizar la escucha multicast
            //Gracias, StackOverflow <3
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if (i.isSiteLocalAddress() && !i.isAnyLocalAddress() && !i.isLinkLocalAddress()
                            && !i.isLoopbackAddress() && !i.isMulticastAddress()) {
                        ms.setNetworkInterface(NetworkInterface.getByName(n.getName()));
                        System.out.println(n.getName());
                    }
                }
            }

            ms.joinGroup(InetAddress.getByName(IP_multicast));

            while (true){
                byte[] buf = new byte[1024];
                DatagramPacket pack = new DatagramPacket(buf, buf.length);
                ms.receive(pack);
                //System.out.println("Received data from: " + pack.getAddress().toString() +
                //        ":" + pack.getPort() + " with length: " +
                //        pack.getLength());
                //System.out.write(pack.getData(), 0, pack.getLength());

                String msg = new String(pack.getData(),0, pack.getLength());
                String[] titan = msg.split(",");
                System.out.println("Aparece nuevo Titan! "+titan[0]+", tipo "+titan[1]+", ID "+titan[2]+".");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}