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



    /** Wires the found-item service to item lookups and Mongo persistence. */
    public foundItemService(MongoTemplate mongoTemplate, itemService iService)
    {
        this.mongoTemplate = mongoTemplate;
        this.iService = iService;
    }

    /**
     * Creates a claim record after verifying the item exists, the item is not
     * already claimed, and the submitted contact fields match the claimant type.
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

    /** Returns every claim record currently stored in MongoDB. */
    public List<foundItem> getAllFoundItems()
    {
        return mongoTemplate.findAll(foundItem.class);
    }

    /** Looks up a single claim record by id. */
    public Optional<foundItem> getFoundItemById(String id)
    {
        var foundItem = mongoTemplate.findById(id, foundItem.class);
        
        return Optional.ofNullable(foundItem);
    }

    /** Updates a saved claim record after rerunning item and contact validation. */
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

    /** Removes a claim record and deletes the linked listing when the claim exists. */
    public boolean deleteFoundItem(String id)
    {
        var found = mongoTemplate.findById(id, foundItem.class);

        if (found == null) return false;

        iService.deleteItem(found.getItemId());

        var query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.remove(query, foundItem.class).getDeletedCount() > 0;
    }

    /** Checks whether the item referenced by the claim is already attached to another claim. */
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
    /**
     * Periodically removes stale claim records and also deletes their matching
     * listings so expired claims do not keep items off the browse page forever.
     */
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

    /** Enforces the contact fields required for either student or guest claims. */
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
