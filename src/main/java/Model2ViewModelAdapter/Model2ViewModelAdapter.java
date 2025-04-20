package Model2ViewModelAdapter;

import org.eclipse.jetty.websocket.api.Session;
import viewModel.viewModel;

import java.io.IOException;
import java.util.Set;

/**
 * The class defining the adapter pattern for interactions from the Model to the ViewModel.
 */
public class Model2ViewModelAdapter{
    //final viewModel vm;
    private final viewModel vm;
    //viewModelBroadcasting viewModelBroadcasting = new viewModelBroadcasting();

    /**
     * Default constructor for a null adapter instance.
     * This allows the ViewModel to use a placeholder adapter before an operational adapter is set.
     */
    public Model2ViewModelAdapter(viewModel vm) {
        // constructor
        //this.vm = new viewModel();
        this.vm = vm;
    }
    public void sendAndReceiveUpdateInfo(String type, String message, Session session, Set<Session> sessions){
        if (type.equalsIgnoreCase("broadcast")) {
            vm.broadcast(message, sessions);
        } else if (type.equalsIgnoreCase("sendTo") && session != null) {
            vm.sendTo(session, message);
        } else {
            System.err.println("Invalid send type or missing session: " + type);
        }
    }

//    public void sendAndReceiveUpdateInfo(String type, String message, Session session, Set<Session> sessions){
//        if (type.equalsIgnoreCase("broadcast")) {
//            //model2ViewModelAdapter.broadcast(message, sessions);
//            viewModelBroadcasting.broadcast(message, sessions);
//        } else if (type.equalsIgnoreCase("sendTo") && session != null) {
//            //model2ViewModelAdapter.sendTo(session, message);
//            viewModelBroadcasting.sendTo(session, message);
//        } else {
//            System.err.println("Invalid send type or missing session: " + type);
//        }
//    }

    public void broadcast(String message, Set<Session> sessions) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getRemote().sendString(message);
                    } catch (IOException e) {
                        System.err.println("Broadcast error: " + e.getMessage());
                    }
                }
            }
        }
    }

    public void sendTo(Session session, String msg) {
        try {
            if (session.isOpen()) {
                session.getRemote().sendString(msg);
            }
            System.out.println("Sending to client: " + msg);
        } catch (IOException e) {
            System.err.println("Send error: " + e.getMessage());
        }
    }

    /**
     * Receives an object from the model and sends updated information to the ViewModel.
     *
     * @param type The type/category of the update.
     * @param informationObject The object containing relevant information to be handled.
     * @return A MessageInformation object containing the result or acknowledgment of the operation.
     */
    public Object receiveAndSendInformation(String type, Object informationObject) {
        //return null;
        return Object.class;//new MessageInformation()
    }

    /**
     * Sends the login status and associated information to the ViewModel.
     *
     * @param success A boolean indicating whether the login attempt was successful.
     * @param informationObject The object containing login/session/user info.
     * @return A MessageInformation object representing the outcome to be broadcast.
     */
    public Object showLoggedInPage(boolean success, Object informationObject) {
        //return null;
        return Object.class;//new MessageInformation();
    }
}