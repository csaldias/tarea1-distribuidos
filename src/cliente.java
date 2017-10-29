import java.io.*;
import java.net.*;
import java.util.*;

public class cliente {
    private int PUERTO_CENTRAL; //Puerto para la conexión
    private String IP_CENTRAL; //Host para la conexión
    private int PUERTO_MULTICAST; //Puerto para multicast
    private String IP_MULTICAST; //IP para multicast
    protected String mensajeServidor; //Mensajes entrantes (recibidos) en el servidor
    protected ServerSocket ss; //Socket del servidor
    protected Socket ds; //Socket distrito
    protected Socket cs; //Socket servidor central
    @SuppressWarnings("Duplicates")
    public void startClient() //Método para iniciar el cliente
    {
        try {

            //conectarse al servidor central
            Scanner scan = new Scanner(System.in);
            System.out.print("Ingrese IP Servidor Central: ");
            IP_CENTRAL = scan.nextLine();
            System.out.print("Ingrese puerto Servidor Central: ");
            PUERTO_CENTRAL = Integer.parseInt(scan.nextLine());

            cs = new Socket(IP_CENTRAL, PUERTO_CENTRAL);

            //datagramas servidor central
            DataOutputStream salidaServidor = new DataOutputStream(cs.getOutputStream());
            DataInputStream entrada = new DataInputStream(cs.getInputStream());

            //conectarse al distrito: Recibir distritos
            System.out.print("\nDistritos disponibles: ");
            String comando = "list";
            salidaServidor.writeUTF(comando);
            mensajeServidor = entrada.readUTF();
            System.out.println(mensajeServidor);
            String[] ldist = mensajeServidor.split(",");
            //conectarse al distrito: seleccionar distritos
            boolean correcto = false;
            String distrito = "";
            while (!correcto) {
                System.out.print("Seleccionar Distrito: ");
                distrito = scan.nextLine();
                if (Arrays.asList(ldist).contains(distrito)){
                    correcto = true;
                }
                else {
                    System.out.println("ERROR: Distrito no encontrado.");
                }
            }
            comando = "connect " + distrito;
            salidaServidor.writeUTF(comando);
            System.out.println("Esperando Autorización...");
            mensajeServidor = entrada.readUTF();
            String[] partes = mensajeServidor.split(",");

            //La conexion fue autorizada?
            String[] msg_stat = partes[0].split(" ");
            if(msg_stat[0].equals("err")) {
                System.out.println("Conexión no autorizada.");
                cs.close();
                System.exit(0);
            }

            System.out.println("Conectando Multicast...");
            IP_MULTICAST = partes[1];
            PUERTO_MULTICAST = Integer.parseInt(partes[2]);
            String IP_DISTRITO = partes[3];
            Integer PUERTO_DISTRITO = Integer.parseInt(partes[4]);

            //conectarse al distrito: conectar servidor
            System.out.println("Conectando Servidor...");
            ds = new Socket(IP_DISTRITO, PUERTO_DISTRITO);

            //conectarse al distrito: datagramas distrito
            DataOutputStream salidaDistrito = new DataOutputStream(ds.getOutputStream());
            DataInputStream entradaDistrito= new DataInputStream(ds.getInputStream());

            //conectarse al distrito: conectar multicast
            MultithreadListen ml = new MultithreadListen(IP_MULTICAST, PUERTO_MULTICAST, 0);
            ml.start();

            //Menu cliente
            String accion;
            int id_capturar;
            int id_asesinar;
            Map<Integer, String> titanes_asesinados = new HashMap<Integer, String>();
            Map<Integer, String> titanes_capturados = new HashMap<Integer, String>();
            String[] resultado;
            while (true) {
                System.out.println("\nOpciones Posibles:");
                System.out.println("1-Listar Titanes.");
                System.out.println("2-Capturar Titan.");
                System.out.println("3-Asesinar Titan.");
                System.out.println("4-Ver Titanes Capturados.");
                System.out.println("5-Ver Titanes Asesinados.");
                System.out.println("6-Cambiar de Distrito.");
                System.out.print("Accion a realizar: ");
                accion = scan.nextLine();

                //Lista de titanes
                switch (accion) {
                    case "1":
                        System.out.println("Titanes actualmente en " + distrito + ":");
                        comando = "list";
                        salidaDistrito.writeUTF(comando);
                        mensajeServidor = entradaDistrito.readUTF();
                        if (mensajeServidor.equals("")){
                            System.out.println("No hay titanes.");
                        }
                        else {
                            String[] titanes = mensajeServidor.split(";");
                            for (String Titan : titanes) {
                                String[] infoTitan = Titan.split(",");
                                System.out.println("Titan: " + infoTitan[1]);
                                switch (infoTitan[2]) {
                                    case "1":
                                        System.out.println("Tipo: Normal");
                                        break;
                                    case "2":
                                        System.out.println("Tipo: Excentrico");
                                        break;
                                    case "3":
                                        System.out.println("Tipo: Cambiante");
                                        break;
                                    default:
                                        System.out.println("ERROR: Algo raro ocurrió. (case 1)");
                                        break;
                                }
                                System.out.println("ID: " + infoTitan[0]);
                                System.out.println("***************");
                            }
                        }
                        break;

                    // Capturar Titan
                    case "2":
                        System.out.print("ID del titan a capturar: ");
                        id_capturar = Integer.parseInt(scan.nextLine());
                        mensajeServidor = "capture " + id_capturar;
                        salidaDistrito.writeUTF(mensajeServidor);
                        mensajeServidor = entradaDistrito.readUTF();
                        resultado = mensajeServidor.split(" ");
                        switch (resultado[0]){
                            case "err":
                                switch (resultado[1]){
                                    case "NoExiste":
                                        System.out.println("ERROR: No se encontró el titán en el distrito");
                                        break;
                                    case "TipoIncompatible":
                                        System.out.println("ERROR: No es posible capturar a este titán.");
                                        break;
                                    default:
                                        System.out.println("ERROR: Algo raro ocurrió (case 2).");
                                        break;

                                }
                                break;
                            case "success":
                                System.out.println("Titan Capturado!");
                                String[] Datos = resultado[1].split(",");
                                String titan_capturado = Datos[1] +","+ Datos[2] +","+ distrito;
                                titanes_capturados.put(id_capturar,titan_capturado);
                                break;
                            default:
                                System.out.println("ERROR: Algo raro ocurrió. (case 2)");
                                break;

                        }
                        break;

                    // Asesinar Titan
                    case "3":
                        System.out.print("ID del titan a asesinar: ");
                        id_asesinar = Integer.parseInt(scan.nextLine());
                        mensajeServidor = "kill " + id_asesinar;
                        salidaDistrito.writeUTF(mensajeServidor);
                        mensajeServidor = entradaDistrito.readUTF();
                        resultado = mensajeServidor.split(" ");
                        switch (resultado[0]){
                            case "err":
                                switch (resultado[1]){
                                    case "NoExiste":
                                        System.out.println("ERROR: No se encontró el titán en el distrito.");
                                        break;
                                    case "TipoIncompatible":
                                        System.out.println("ERROR: No es posible asesinar a este titán.");
                                        break;
                                    default:
                                        System.out.println("ERROR: Algo raro ocurrió. (case 3)");
                                        break;

                                }
                                break;
                            case "success":
                                System.out.println("Titan Asesinado!");
                                String[] Datos = resultado[1].split(",");
                                String titan_asesinado = Datos[1] +","+ Datos[2] +","+ distrito;
                                titanes_asesinados.put(id_asesinar,titan_asesinado);
                                break;
                            default:
                                System.out.println("Algo raro ocurrió. (case 3)");
                                break;

                        }
                        break;

                    // Ver Titanes Capturados
                    case "4":
                        System.out.println("Titanes Capturados:");
                        for (Map.Entry<Integer, String> entry : titanes_capturados.entrySet()) {
                            resultado = entry.getValue().split(",");
                            System.out.println("ID: "+ entry.getKey());
                            System.out.println("Nombre: "+ resultado[0]);
                            System.out.println("Distrito de origen: "+ resultado[2]);
                            switch (resultado[1]) {
                                case "1":
                                    System.out.println("Tipo: Normal");
                                    break;
                                case "2":
                                    System.out.println("Tipo: Excentrico");
                                    break;
                                case "3":
                                    System.out.println("Tipo: Cambiante");
                                    break;
                                default:
                                    System.out.println("ERROR: Algo raro ocurrió. (case 4)");
                                    break;
                            }
                            System.out.println("***************");
                        }
                        break;

                    // Ver Titanes Asesinados
                    case "5":
                        System.out.println("Titanes Asesinados:");
                        for (Map.Entry<Integer, String> entry : titanes_asesinados.entrySet()) {
                            resultado = entry.getValue().split(",");
                            System.out.println("ID: "+ entry.getKey());
                            System.out.println("Nombre: "+ resultado[0]);
                            System.out.println("Distrito de origen: "+ resultado[2]);
                            switch (resultado[1]) {
                                case "1":
                                    System.out.println("Tipo: Normal");
                                    break;
                                case "2":
                                    System.out.println("Tipo: Excentrico");
                                    break;
                                case "3":
                                    System.out.println("Tipo: Cambiante");
                                    break;
                                default:
                                    System.out.println("ERROR: Algo raro ocurrió. (case 5)");
                                    break;
                            }
                            System.out.println("***************");
                        }
                        break;

                    // Cambiar Distrito
                    case "6":
                        System.out.println("Desconectando de "+distrito+ "...");
                        ds.close();
                        ml.interrupt();
                        System.out.print("\nDistritos disponibles: ");
                        comando = "list";
                        salidaServidor.writeUTF(comando);
                        mensajeServidor = entrada.readUTF();
                        System.out.println(mensajeServidor);

                        //seleccionar distritos
                        System.out.print("Seleccionar Distrito: ");
                        distrito = scan.nextLine();
                        comando = "connect " + distrito;
                        salidaServidor.writeUTF(comando);
                        System.out.println("Esperando Autorización...");
                        mensajeServidor = entrada.readUTF();
                        partes = mensajeServidor.split(",");

                        //La conexion fue autorizada?
                        String[] msg_status = partes[0].split(" ");
                        if(msg_status[0].equals("err")) {
                            System.out.println("Conexión no autorizada.");
                            cs.close();
                            System.exit(0);
                        }

                        System.out.println("Conectando Multicast...");
                        IP_MULTICAST = partes[1];
                        PUERTO_MULTICAST = Integer.parseInt(partes[2]);
                        IP_DISTRITO = partes[3];
                        PUERTO_DISTRITO = Integer.parseInt(partes[4]);

                        //conectarse al distrito: conectar servidor
                        System.out.println("Conectando Servidor...");
                        ds = new Socket(IP_DISTRITO, PUERTO_DISTRITO);

                        //conectarse al distrito: datagramas distrito
                        salidaDistrito = new DataOutputStream(ds.getOutputStream());
                        entradaDistrito= new DataInputStream(ds.getInputStream());

                        //conectarse al distrito: conectar multicast
                        ml = new MultithreadListen(IP_MULTICAST, PUERTO_MULTICAST, 0);
                        ml.start();

                        System.out.println("Conexión a "+distrito+"completada.");

                        break;

                    //Algo raro ocurrió
                    default:
                        System.out.println("Algo raro ocurrió");
                        break;
                }
                System.out.print("\nFinalizado, presione Enter para continuar.");
                scan.nextLine();
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
    private MulticastSocket ms;

    MultithreadListen (String ip, int puerto, int i) {
        IP_multicast = ip;
        puerto_multicast = puerto;
        clientID = i;
    }

    public void run() {
        try {
            ms = new MulticastSocket(puerto_multicast);

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
                System.out.println("\n-------------------------------------------------------------");
                System.out.println("[Titan] Aparece nuevo Titan! "+titan[0]+", tipo "+titan[1]+", ID "+titan[2]+".");
                System.out.println("-------------------------------------------------------------");
            }

        }
        catch (SocketException e){
            //Para cuando terminamos el multithread
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interrupt(){
        super.interrupt();
        this.ms.close();
    }
}