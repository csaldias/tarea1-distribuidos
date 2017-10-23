import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class cliente {

    private int PUERTO = 1234; //Puerto para la conexión
    private String HOST = "localhost"; //Host para la conexión
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