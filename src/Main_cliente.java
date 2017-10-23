import java.io.IOException;

public class Main_cliente {
    public static void main(String[] args) throws IOException
    {
        cliente cli = new cliente(); //Se crea el cliente
        System.out.println("Iniciando cliente\n");
        cli.startClient(); //Se inicia el cliente
    }
}
