import java.io.IOException;

//Clase principal que hará uso del servidor
public class Main_servidor_central {
    public static void main(String[] args) throws IOException
    {
        servidor_central serv = new servidor_central(); //Se crea el servidor

        System.out.println("Iniciando servidor central\n");
        serv.startServer(); //Se inicia el servidor
    }
}