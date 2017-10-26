import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class servidor_central {
    private int PUERTO = 1234; //Puerto para la conexión

    public void startServer()//Método para iniciar el servidor
    {
        try {
            //Vamos a agregar distritos?
            System.out.print("Agregar servidores de distrito?[S/N] ");
            Scanner scan = new Scanner(System.in);
            String opcion = scan.nextLine();

            while(!opcion.equals("N")) {
                //Agreguemos más servidores de distritos!
                Path db_distrito = Paths.get("db_distrito.csv");
                System.out.print("Nombre distrito: ");
                String nombre = scan.nextLine();
                System.out.print("IP Multicast: ");
                String ip_multicast = scan.nextLine();
                System.out.print("Puerto Multicast: ");
                String puerto_multicast = scan.nextLine();
                System.out.print("IP Peticiones: ");
                String ip_peticiones = scan.nextLine();
                System.out.print("Puerto Peticiones: ");
                String puerto_peticiones = scan.nextLine();

                List<String> nuevo_sv = Arrays.asList(nombre+","+ip_multicast+","+puerto_multicast+","+ip_peticiones+","+puerto_peticiones);
                Files.write(db_distrito, nuevo_sv, Charset.forName("UTF-8"), StandardOpenOption.APPEND);

                System.out.print("Agregar más servidores de distrito?[S/N] ");
                opcion = scan.nextLine();
                System.out.println(opcion);
            }

            //En qué puerto queremos correr el servidor?
            System.out.print("Puerto para peticiones? ");
            String text = scan.nextLine();
            PUERTO = Integer.parseInt(text);

            ServerSocket ss = new ServerSocket(PUERTO);
            int id=0;
            System.out.println("Esperando en puerto "+text+"..."); //Esperando conexión
            while (true) {
                Socket cs = ss.accept(); //Accept comienza el socket y espera una conexión desde un cliente
                ClientServiceThread cliThread = new ClientServiceThread(cs, id++);
                cliThread.start();
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}


class ClientServiceThread extends Thread {
    Socket cs;
    int clientID = -1;
    public String mensajeServidor; //Mensaje entrante (recibido) por el servidor
    static int titan_id = 100; //ID para titanes (asignacion)

    ClientServiceThread(Socket s, int i) {
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

                    } else if (mensaje[0].equals("id_titan")) {
                        //Devolvemos ID para el titan
                        salidaCliente.writeUTF(Integer.toString(titan_id++));
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