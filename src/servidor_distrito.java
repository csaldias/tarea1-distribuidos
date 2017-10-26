import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.MulticastSocket;
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
            System.out.print("Puerto para peticiones: ");
            PUERTO_PETICION = Integer.parseInt(scan.nextLine());
            //No pedimos IP, porque por defecto escuchamos peticiones en todas las interfaces de la máquina

            ServerSocket ss = new ServerSocket(PUERTO_PETICION);

            InetAddress group = InetAddress.getByName(IP_MULTICAST);
            MulticastSocket ms = new MulticastSocket(PUERTO_MULTICAST);
            int id=0;
            System.out.println("Esperando mensajes en puerto "+PUERTO_PETICION+"..."); //Esperando conexión

            //Iniciamos thread de titanes
            ClientServiceThreadTitanes titanThread = new ClientServiceThreadTitanes(ms, id++);
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
            //Obtenemos la ubicación de nuestra BD de titanes
            Path db_distrito = Paths.get("db_titanes.csv");
            //Obtenemos la IP del cliente
            String ip = cs.getInetAddress().toString().replace("/", "");

            while ((mensajeServidor = entrada.readUTF()) != null) { //Mientras existan mensajes del cliente...
                String[] mensaje = mensajeServidor.split(" ");

                if (!mensajeServidor.isEmpty()){
                    System.out.println("Recibido: "+mensajeServidor);

                    switch (mensaje[0]) {
                        case "list":
                            System.out.println("LIST command received.");
                            //Listamos los titanes disponibles en el distrito
                            String distritos = "";
                            String readLine = "";
                            BufferedReader b = new BufferedReader(new FileReader(db_distrito.toFile()));
                            while ((readLine = b.readLine()) != null) {
                                distritos += readLine.split(",")[0] + ",";
                            }
                            distritos = distritos.substring(0, distritos.length() - 1);
                            System.out.println(distritos);
                            salidaCliente.writeUTF(distritos);
                            break;
                        case "capture":
                            //do something else
                            System.out.println("CAPTURE command received.");

                            break;
                        case "kill":
                            //do something else
                            System.out.println("KILL command received.");

                            break;
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

class ClientServiceThreadTitanes extends Thread {
    MulticastSocket ms;
    int clientID = -1;
    public String mensajeServidor; //Mensaje entrante (recibido) por el servidor

    ClientServiceThreadTitanes (MulticastSocket s, int i) {
        ms = s;
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
            System.out.println("Thread Titanes");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}