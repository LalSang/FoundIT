package foundIT.demo.service;


import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import foundIT.demo.model.foundItem;

@Service
public class foundItemService
{
    private final itemService iService;
    private final MongoTemplate mongoTemplate;



    public foundItemService(MongoTemplate mongoTemplate, itemService iService)
    {
        this.mongoTemplate = mongoTemplate;
        this.iService = iService;
    }

    public foundItem createFoundItem(foundItem i)
    {
        if((iService.getItemById(i.getItemId())).isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid admin");
        }

        if(i.getIsAppUser())
        {
            if((i.getAppId()).isEmpty())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Need AppID for App User");
            }
        }
        else
        {
            if((i.getPhoneNum()).isEmpty() || (i.getEmail()).isEmpty())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both email and phone num must be filled for Guests");
            }
        }

        return mongoTemplate.save(i);
    }

    public List<foundItem> getAllFoundItems()
    {
        return mongoTemplate.findAll(foundItem.class);
    }

    public Optional<foundItem> getFoundItemById(String id)
    {
        var foundItem = mongoTemplate.findById(id, foundItem.class);
        
        return Optional.ofNullable(foundItem);
    }

    public foundItem updateFoundItem(String id, foundItem i)
    {
        if((iService.getItemById(i.getItemId())).isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid admin");
        }

        if(i.getIsAppUser())
        {
            if((i.getAppId()).isEmpty())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Need AppID for App User");
            }
        }
        else
        {
            if((i.getPhoneNum()).isEmpty() || (i.getEmail()).isEmpty())
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both email and phone num must be filled for Guests");
            }
        }

        var query = new Query(Criteria.where("id").is(id));

        Update updatedFoundItem = new Update()
                            .set("itemId", i.getItemId())
                            .set("firstName", i.getFirstName())
                            .set("lastName", i.getLastName())
                            .set("isAppUSer", i.getIsAppUser())
                            .set("appId", i.getAppId())
                            .set("phoneNum", i.getPhoneNum())
                            .set("email", i.getEmail());
        
        mongoTemplate.updateFirst(query, updatedFoundItem, foundItem.class);
        return getFoundItemById(id).orElse(null);
    }

    public boolean deleteFoundItem(String id)
    {
        var query =new Query(Criteria.where("id").is(id));
        return mongoTemplate.remove(query, foundItem.class).getDeletedCount() > 0;
    }

}