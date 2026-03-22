package foundIT.demo.service;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import foundIT.demo.model.foundItem;

@Service
public class foundItemService
{
    private final itemService iService;
    private final MongoTemplate mongoTemplate;




    /**foundItemService
     *  Class Constructor
     * 
     * @param mongoTemplate - The template for the mongo database
     * @param iService - ItemService object so this class is able to run itemService functions
     */
    public foundItemService(MongoTemplate mongoTemplate, itemService iService)
    {
        this.mongoTemplate = mongoTemplate;
        this.iService = iService;
    }



    /**createFoundItem
     *  Creates an item and adds it to the database
     * 
     * @param i - The Founditem to be added
     * @return - The item if successfully added nothing if not
     */
    public foundItem createFoundItem(foundItem i)
    {
        if((iService.getItemById(i.getItemId())).isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item record not found");
        }

        if(itemFound(i))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A claim has already been started for this item");
        }

        validateContactInfo(i);

        if(i.getIsAppUser())
        {
            if(i.getAppId() == null || i.getAppId().isBlank())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student ID, phone number, and email are required for student claims");
            }
        }

        if(i.getDate() == null)
        {
            i.setDate(Instant.now());
        }

        return mongoTemplate.save(i);
    }

    /**getAllFoundItems
     *  Return a list of all items in the database
     * 
     * @return - A list of Finditems
     */
    public List<foundItem> getAllFoundItems()
    {
        return mongoTemplate.findAll(foundItem.class);
    }

    /**getFoundItemById
     * 
     * @param id - foundItem identifier 
     * @return - specific foundItem
     */
    public Optional<foundItem> getFoundItemById(String id)
    {
        var foundItem = mongoTemplate.findById(id, foundItem.class);
        
        return Optional.ofNullable(foundItem);
    }

    /**
     * 
     * @param id - foundItem identifier 
     * @param i - foundItem object
     * @return - specific foundItem with updates
     */ 
    public foundItem updateFoundItem(String id, foundItem i)
    {
        if((iService.getItemById(i.getItemId())).isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item record not found");
        }

        validateContactInfo(i);

        if(i.getIsAppUser())
        {
            if(i.getAppId() == null || i.getAppId().isBlank())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student ID, phone number, and email are required for student claims");
            }
        }

        var query = new Query(Criteria.where("id").is(id));

        Update updatedFoundItem = new Update()
                            .set("itemId", i.getItemId())
                            .set("firstName", i.getFirstName())
                            .set("lastName", i.getLastName())
                            .set("isAppUser", i.getIsAppUser())
                            .set("appId", i.getAppId())
                            .set("phoneNum", i.getPhoneNum())
                            .set("email", i.getEmail())
                            .set("date", i.getDate() == null ? Instant.now() : i.getDate());
        
        mongoTemplate.updateFirst(query, updatedFoundItem, foundItem.class);
        return getFoundItemById(id).orElse(null);
    }

    /**deleteFoundItem
     * 
     * @param id - foundItem identification 
     * @return - True if foundItem is found and deleted
     */
    public boolean deleteFoundItem(String id)
    {
        var found = mongoTemplate.findById(id, foundItem.class);

        if (found == null) return false;

        iService.deleteItem(found.getItemId());

        var query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.remove(query, foundItem.class).getDeletedCount() > 0;
    }

    /**
     * itemFouns
     * @param ii - foundItem object
     * @return - true if item is found
     */
    public boolean itemFound(foundItem ii)
    {
        List<foundItem> li = getAllFoundItems();
        for ( foundItem f : li)
        {
            if (f.getItemId().equals(ii.getItemId()))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * cleanupExpiredFoundItems
     * chekcs every 60 sec if a foundItem hit the cutoffTime
     */
    @Scheduled(fixedRate = 60000) // runs every 60 seconds
    public void cleanupExpiredFoundItems()
    {

        //for testing
        Instant cutOff = Instant.now().minusSeconds(120);

        //for 5 days
        //Instant cutOff = Instant.now();

        Query query = new Query(Criteria.where("date").lt(cutOff));

        List<foundItem> expiredItems = mongoTemplate.find(query, foundItem.class);

        for (foundItem foundItem : expiredItems) 
        {
            iService.deleteItem(foundItem.getItemId());
            mongoTemplate.remove(foundItem);
        }
    }

    /**
     * validateContatInfo
     * @param i - foundItemObject
     * 
     * checks for empty required info
     */
    private void validateContactInfo(foundItem i)
    {
        if(i.getPhoneNum() == null || i.getPhoneNum().isBlank() || i.getEmail() == null || i.getEmail().isBlank())
        {
            if(i.getIsAppUser())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student ID, phone number, and email are required for student claims");
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number and email are required for guest claims");
        }
    }
}
