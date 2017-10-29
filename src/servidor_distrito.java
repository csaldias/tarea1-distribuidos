import com.sun.xml.internal.ws.api.pipe.SOAPBindingCodec;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class servidor_distrito {
    private int PUERTO_PETICION = 1234; //Puerto para la conexión
    private String IP_MULTICAST = "0.0.0.0"; //IP para mensajes Multicast
    private int PUERTO_MULTICAST = 1234; //Puerto para mensajes Multicast
    private String IP_CENTRAL = "0.0.0.0"; //IP del Servidor Central
    private int PUERTO_CENTRAL = 1234; //Puerto del Servidor Central

    public void startServer()//Método para iniciar el servidor
    {
        try {
            //Obtengamos los parámetros necesarios para inicializar el servidor
            Scanner scan = new Scanner(System.in);
            //Obtenemos IP y Puerto para contactar a Servidor Central
            System.out.print("IP Servidor Central: ");
            IP_CENTRAL = scan.nextLine();
            System.out.print("Puerto Servidor Central: ");
            PUERTO_CENTRAL = Integer.parseInt(scan.nextLine());

            //Obtenemos IP y Puerto para mensajes Multicast
            System.out.print("IP Multicast: ");
            IP_MULTICAST = scan.nextLine();
            System.out.print("Puerto Multicast: ");
            PUERTO_MULTICAST = Integer.parseInt(scan.nextLine());

            //Obtenemos puerto para peticiones de titanes
            System.out.print("Puerto para peticiones: ");
            PUERTO_PETICION = Integer.parseInt(scan.nextLine());
            //No pedimos IP, porque por defecto escuchamos peticiones en todas las interfaces de la máquina

            ServerSocket ss = new ServerSocket(PUERTO_PETICION);

            Socket central = new Socket(IP_CENTRAL, PUERTO_CENTRAL);

            int id=0;

            System.out.println("Esperando mensajes en puerto "+PUERTO_PETICION+"..."); //Esperando conexión

            //Iniciamos thread de titanes
            ClientServiceThreadTitanes titanThread = new ClientServiceThreadTitanes(IP_MULTICAST, PUERTO_MULTICAST, central, id++);
            titanThread.start();

            //Esperamos conexiones de peticiones, iniciamos thread por cada conexion
            while (true) {
                Socket cs = ss.accept(); //Accept comienza el socket y espera una conexión desde un cliente
                ClientServiceThreadRequests cliThread = new ClientServiceThreadRequests(cs, id++);
                cliThread.start();
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}


class ClientServiceThreadRequests extends Thread {
    Socket cs;
    int clientID = -1;
    public String mensajeServidor; //Mensaje entrante (recibido) por el servidor
    static public Map<Integer, String> bd_titanes = new HashMap<Integer, String>(); //BD de Titanes

    ClientServiceThreadRequests(Socket s, int i) {
        cs = s;
        clientID = i;
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public void run() {
        try {
            //Se obtiene el flujo de salida del cliente para enviarle mensajes (servidor -> cliente)
            DataOutputStream salidaCliente = new DataOutputStream(cs.getOutputStream());
            //Se obtiene el flujo entrante desde el cliente (cliente -> servidor)
            DataInputStream entrada = new DataInputStream(cs.getInputStream());
            //Obtenemos la IP del cliente
            String ip = cs.getInetAddress().toString().replace("/", "");

            while ((mensajeServidor = entrada.readUTF()) != null) { //Mientras existan mensajes del cliente...
                String[] mensaje = mensajeServidor.split(" ");

                if (!mensajeServidor.isEmpty()){
                    switch (mensaje[0]) {
                        case "list":
                            System.out.println("LIST command received");
                            //Listamos los titanes disponibles en el distrito
                            String lista_titanes = "";

                            for (Map.Entry<Integer, String> titan : ClientServiceThreadRequests.bd_titanes.entrySet()) {
                                lista_titanes += titan.getKey() + "," + titan.getValue() + ";";
                            }
                            //System.out.println(lista_titanes);
                            salidaCliente.writeUTF(lista_titanes);
                            //Ejemplo de mensaje enviado a cliente:
                            //100,Eren,1;101,Carlitos,3;102,Camilo,2;

                            break;
                        case "capture":
                            System.out.println("CAPTURE command received.");
                            //Capturamos a un titán, sólo si su tipo es 1 o 3

                            //Existe el titan a capturar?
                            if(!ClientServiceThreadRequests.bd_titanes.containsKey(Integer.parseInt(mensaje[1]))) {
                                //System.out.println("Titán inexistente.");
                                salidaCliente.writeUTF("err NoExiste");
                            } else {
                                //Obtenemos el titan a capturar
                                String titan = ClientServiceThreadRequests.bd_titanes.get(Integer.parseInt(mensaje[1]));

                                //El titan es de tipo 1 o 3?
                                if(!(titan.split(",")[1].equals("1") || titan.split(",")[1].equals("3"))) {
                                    salidaCliente.writeUTF("err TipoIncompatible");
                                } else {
                                    //Se captura al titan
                                    ClientServiceThreadRequests.bd_titanes.remove(Integer.parseInt(mensaje[1]));
                                    salidaCliente.writeUTF("success "+mensaje[1]+","+titan);
                                    //Ejemplo de mensaje enviado a cliente:
                                    //success 100,Eren,1
                                }
                            }

                            break;
                        case "kill":
                            //do something else
                            System.out.println("KILL command received.");
                            //Asesinamos a un titán, sólo si su tipo es 1 o 2

                            //Existe el titan a capturar?
                            if(!ClientServiceThreadRequests.bd_titanes.containsKey(Integer.parseInt(mensaje[1]))) {
                                //System.out.println("Titán inexistente.");
                                salidaCliente.writeUTF("err NoExiste");
                            } else {
                                //Obtenemos el titan a capturar
                                String titan = ClientServiceThreadRequests.bd_titanes.get(Integer.parseInt(mensaje[1]));

                                //El titan es de tipo 1 o 2?
                                if(!(titan.split(",")[1].equals("1") || titan.split(",")[1].equals("2"))) {
                                    salidaCliente.writeUTF("err TipoIncompatible");
                                } else {
                                    //Se asesina al titan
                                    ClientServiceThreadRequests.bd_titanes.remove(Integer.parseInt(mensaje[1]));
                                    salidaCliente.writeUTF("success "+mensaje[1]+","+titan);
                                    //Ejemplo de mensaje enviado a cliente:
                                    //success 100,Eren,1
                                }
                            }

                            break;
                    }
                }
            }

        }
        catch (EOFException e) {
            System.out.println("Cliente desconectado.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

class ClientServiceThreadTitanes extends Thread {
    String IP_multicast;
    int puerto_multicast;
    Socket cs;
    int clientID = -1;
    public String mensajeServidor; //Mensaje entrante (recibido) por el servidor

    ClientServiceThreadTitanes (String ip, int puerto, Socket c, int i) {
        IP_multicast = ip;
        puerto_multicast = puerto;
        cs = c;
        clientID = i;
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public void run() {
        try {
            InetAddress group = InetAddress.getByName(IP_multicast);
            MulticastSocket ms = new MulticastSocket();
            //ms.joinGroup(group);

            DataOutputStream salidaServidor = new DataOutputStream(cs.getOutputStream());
            DataInputStream entrada = new DataInputStream(cs.getInputStream());
            Scanner scan = new Scanner(System.in);

            while (true){
                System.out.println("Para publicar titanes, presione Enter.");
                String text = scan.nextLine();
                if (text.isEmpty()) {
                    System.out.print("Nombre del Titán: ");
                    String nombre_titan = scan.nextLine();
                    System.out.print("Tipo Iitán [1: Normal, 2: Excéntrico, 3: Cambiante]: ");
                    int tipo_titan = Integer.parseInt(scan.nextLine());

                    //Hacemos request por el ID de titán
                    salidaServidor.writeUTF("id_titan");
                    int id_titan = Integer.parseInt(entrada.readUTF());

                    //Publicamos el nuevo titán
                    String msg = nombre_titan + "," + tipo_titan + "," + id_titan;
                    DatagramPacket hi = new DatagramPacket(msg.getBytes("UTF-8"), msg.getBytes("UTF-8").length,
                            group, puerto_multicast);
                    ms.send(hi);

                    //Agregamos el nuevo titan a la lista
                    //List<String> nuevo_titan = Arrays.asList(msg);
                    //Files.write(db_titanes, nuevo_titan, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
                    ClientServiceThreadRequests.bd_titanes.put(id_titan, nombre_titan+","+tipo_titan);

                    System.out.println("Titán publicado.\n");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}