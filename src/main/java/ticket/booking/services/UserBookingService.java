package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserBookingService {
    private User user;
    private List<User> userList;
    private ObjectMapper objectMapper = new ObjectMapper();
    {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
    // keeping those static as this shd always be in memory for use
    // for direct usage in other classes
    private static final String USERS_PATH="/Users/sam/Desktop/IRCTC/app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user1) throws IOException {
        this.user=user1;
        loadUsers();

    }

    public UserBookingService() throws IOException {
        loadUsers();
    }

    public static List<Train> getTrains(String src, String dest) {
        // make sure that first source occurs and then dest occurs in the trains
        try {
            TrainService trainService = new TrainService();
            List<Train> validTrains = trainService.searchTrains(src, dest);
            return validTrains;
        }catch(IOException ex){
            return new ArrayList<>();
        }
    }

    public List<User> loadUsers() throws IOException{
        File users=new File(USERS_PATH);
        userList=objectMapper.readValue(users, new TypeReference<List<User>>() {
        });
        return userList;
    }



    public Boolean loginUser(){
        Optional<User> foundUser=userList.stream().filter(user1 ->
                {
                return user1.getName().equalsIgnoreCase(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(),user1.getHashedPassword());
                }).findFirst();
        if(foundUser.isPresent()){
            this.user = foundUser.get();
            return true;
        }

        return false;
    }

    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch(IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException{
        File usersFile=new File(USERS_PATH);
        objectMapper.writeValue(usersFile,userList);
    }

    public void fetchBooking() {

        if (user != null) {
            user.printTickets();
        }else{
            System.out.println("Invalid login credentials, cannot fetch booking");
        }
    }

    public Boolean cancelBooking(String ticketId) throws IOException{
        List<Ticket> ticketsBooked=user.getTicketsBooked();
        Optional<Ticket> ticketCancelled = ticketsBooked.stream().filter(ticket->ticket.getTicketId().equalsIgnoreCase(ticketId)).findFirst();
        if(ticketCancelled.isPresent()){
            TrainService trainService = new TrainService();
           // trainService.cancelBooking(
            ticketsBooked = ticketsBooked.stream()
                    .filter(ticket -> !ticketId.equals(ticket.getTicketId()))
                    .collect(Collectors.toList());
            user.setTicketsBooked(ticketsBooked);
            saveUserListToFile();
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    public Boolean bookTrainSeat(Train selectedTrainForBooking, String src, String dest, String dateOfTravel, int row, int seat) {
        // we would have to use the train service to book the train through the user booking service
        try {
            TrainService trainService = new TrainService();
            if(trainService.canBookSeat(selectedTrainForBooking,row,seat)){
                Ticket ticket=new Ticket(UUID.randomUUID().toString(),user.getUserId(),src,dest,dateOfTravel,selectedTrainForBooking);
                List<Ticket> ticketsBooked = user.getTicketsBooked();
                ticketsBooked.add(ticket);
                user.setTicketsBooked(ticketsBooked);
                saveUserListToFile();
                return true;
            }
            return false;
        }catch(IOException ex){
            return false;
        }
    }
}
