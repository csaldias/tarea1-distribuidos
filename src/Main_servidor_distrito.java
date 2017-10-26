import java.io.IOException;

//Clase principal que hará uso del servidor
public class Main_servidor_distrito {
    public static void main(String[] args) throws IOException
    {
        servidor_distrito serv = new servidor_distrito(); //Se crea el servidor

        System.out.println("Iniciando servidor central\n");
        serv.startServer(); //Se inicia el servidor
    }
}