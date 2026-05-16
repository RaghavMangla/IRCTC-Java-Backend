package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {
    private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();
    {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
    private static final String TRAINS_PATH="/Users/sam/Desktop/IRCTC/app/src/main/java/ticket/booking/localDb/trains.json";
    public TrainService() throws IOException {
        loadTrains();
    }

    public void loadTrains() throws IOException {
        File trains = new File(TRAINS_PATH);
        trainList  = objectMapper.readValue(trains, new TypeReference<List<Train>>() {
        });


        }

        public List<Train> searchTrains(String src, String dest){
           return trainList.stream().filter(train->validTrain(train,src,dest)).collect(Collectors.toList());
        }

    public boolean validTrain(Train train, String src, String dest) {
          List<String> stationOrder = train.getStations();
          int srcIndex= stationOrder.indexOf(src.toLowerCase());
          int destIndex = stationOrder.indexOf(dest.toLowerCase());

          return srcIndex!=-1 && destIndex!=-1 && srcIndex<destIndex;
    }


    public boolean canBookSeat(Train selectedTrain, int row, int seat) {
        List<List<Integer>> seats = selectedTrain.getSeats();

        if(row>=0 && row<seats.size() && seat>=0 && seat<seats.get(row).size() && seats.get(row).get(seat)==0){
            seats.get(row).set(seat,1);
            selectedTrain.setSeats(seats);
            this.addTrain(selectedTrain);
            return true;
        }
        return false;
    }

    public void addTrain(Train newTrain) {
        Optional<Train> existingTrain = trainList.stream().filter(train -> train.getTrainId().equalsIgnoreCase(newTrain.getTrainId())).findFirst();

        if(existingTrain.isPresent()){
            updateTrain(newTrain);
        }else{
            trainList.add(newTrain);
            saveTrainListToFile();
        }
    }

    public void updateTrain(Train updatedTrain) {
        OptionalInt index = IntStream.range(0,trainList.size()).filter(i->trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId())).findFirst();

        if(index.isPresent()){
            trainList.set(index.getAsInt(),updatedTrain);
            saveTrainListToFile();
        }else{
            addTrain(updatedTrain);
        }
    }

    public void saveTrainListToFile() {
        try{
            objectMapper.writeValue(new File(TRAINS_PATH),trainList);
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
