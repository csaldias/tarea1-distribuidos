import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
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

    public void startServer()//Método para iniciar el servidor
    {
        try {
            //Obtengamos los parámetros necesarios para inicializar el servidor
            Scanner scan = new Scanner(System.in);
            //Obtenemos IP y Puerto para mensajes Multicast
            System.out.print("IP Multicast: ");
            IP_MULTICAST = scan.nextLine();
            System.out.print("Puerto Multicast: ");
            PUERTO_MULTICAST = Integer.parseInt(scan.nextLine());
            System.out.print("Puerto para peticiones? ");
            PUERTO_PETICION = Integer.parseInt(scan.nextLine());
            //No pedimos IP, porque por defecto escuchamos peticiones en todas las interfaces de la máquina

            ServerSocket ss = new ServerSocket(PUERTO_PETICION);
            int id=0;
            System.out.println("Esperando mensajes en puerto "+PUERTO_PETICION+"..."); //Esperando conexión
            while (true) {
                Socket cs = ss.accept(); //Accept comienza el socket y espera una conexión desde un cliente
                ClientServiceThread2 cliThread = new ClientServiceThread2(cs, id++);
                cliThread.start();
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}


class ClientServiceThread2 extends Thread {
    Socket cs;
    int clientID = -1;
    public String mensajeServidor; //Mensaje entrante (recibido) por el servidor
    static int titan_id = 1; //ID para titanes (asignacion)

    ClientServiceThread2(Socket s, int i) {
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
            //Obtenemos la ubicación de nuestra BD de servidores de distrito
            Path db_distrito = Paths.get("db_distrito.csv");
            //Obtenemos la IP del cliente
            String ip = cs.getInetAddress().toString().replace("/", "");

            while ((mensajeServidor = entrada.readUTF()) != null) { //Mientras existan mensajes del cliente...
                String[] mensaje = mensajeServidor.split(" ");

                if (!mensajeServidor.isEmpty()){
                    System.out.println("Recibido: "+mensajeServidor);

                    if (mensaje[0].equals("list")) {
                        System.out.println("LIST command received.");
                        //Leemos y parseamos la BD de distritos
                        String distritos = "";
                        String readLine = "";
                        BufferedReader b = new BufferedReader(new FileReader(db_distrito.toFile()));
                        while ((readLine = b.readLine()) != null) {
                            distritos += readLine.split(",")[0]+",";
                        }
                        distritos = distritos.substring(0, distritos.length()-1);
                        System.out.println(distritos);
                        salidaCliente.writeUTF(distritos);
                    } else if (mensaje[0].equals("connect")) {
                        //do something else
                        System.out.println("CONNECT command received.");
                        System.out.print("Autorizar conexion de "+ip+" al servidor "+mensaje[1]+"? [S/N] ");
                        Scanner scan = new Scanner(System.in);
                        String opcion = scan.nextLine();
                        if(opcion.equals("S")) {
                            System.out.println("Petción aceptada.");
                            //Leemos y parseamos la BD de distritos
                            String readLine = "";
                            BufferedReader b = new BufferedReader(new FileReader(db_distrito.toFile()));
                            while ((readLine = b.readLine()) != null) {
                                if (readLine.split(",")[0].equals(mensaje[1])){
                                    salidaCliente.writeUTF("success "+readLine);
                                    break;
                                }
                            }
                        } else {
                            System.out.println("Conexion rechazada.");
                            salidaCliente.writeUTF("err denied");
                        }

                    }
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}