package foundIT.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import foundIT.demo.model.item;
import foundIT.demo.service.itemService;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/items")
public class itemController {
    

    private final itemService iService;


<<<<<<< HEAD
    /**
     * @param iService - itemService for functions
     */
=======
    /** Stores the item service used by this REST controller. */
>>>>>>> 191c937 (comments for main.js)
    public itemController(itemService iService)
    {
        this.iService = iService;
    }

<<<<<<< HEAD
    // Create - Post /api/items
    /**
     * creates a itemObject
     * @param i - item object 
     * @return - 201 if successful
     */
=======
    /** Creates a new item listing from the request body. */
>>>>>>> 191c937 (comments for main.js)
    @PostMapping
    public ResponseEntity<item> createItem(@Valid @RequestBody item i)
    {
        var savedItem = iService.createItem(i);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

<<<<<<< HEAD
    // Read - Get /api/items
    /**
     * returns all items
     * @return - 200 if successful
     */
=======
    /** Returns every saved item listing. */
>>>>>>> 191c937 (comments for main.js)
    @GetMapping
    public ResponseEntity<List<item>> getAllItems()
    {
        List<item> itemList = iService.getAllItems();

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }

    /** Returns only the items that do not already have a claim record. */
    @GetMapping("/unclaimed")
    public ResponseEntity<List<item>> getUnclaimedItems()
    {
        List<item> itemList = iService.getUnclaimedItems();

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }

    /** Returns items whose type matches the incoming query value. */
    @GetMapping("/getType")
    public ResponseEntity<List<item>> getType(String type)
    {
        List<item> itemList = iService.getType(type);

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }

    // Read - Get /api/items/getNotClaimed
    // @GetMapping("/getNotClaimed")
    // public ResponseEntity<List<item>> getNotFound()
    // {
    //     List<item> itemList = iService.getNotFound();

    //     return ResponseEntity.status(HttpStatus.OK).body(itemList);
    // }

    /** Returns items that currently appear on the claimed page. */
    @GetMapping("/getClaimed")
    public ResponseEntity<List<item>> getClaimed()
    {
        List<item> itemList = iService.getClaimed();

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }


    /** Returns one item by id. */
    @GetMapping("/{id}")
    public ResponseEntity<item> updateItem(@PathVariable String id)
    {
        item i = iService.getItemById(id).orElse(null);
        return ResponseEntity.status(HttpStatus.OK).body(i);
    }
    
    /** Updates an existing item when the target id is present. */
    @PutMapping("/{id}")
    public ResponseEntity<item> updatedItem(@PathVariable String id,
                                                @RequestBody item i)
    {
        if(!iService.getItemById(id).isPresent())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var updatedItem = iService.updateItem(id, i);
        return ResponseEntity.status(HttpStatus.OK).body(updatedItem);
    }

    /** Deletes an item and returns a simple success or failure message. */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable String id)
    {
        var deleted = iService.deleteItem(id);
        return deleted ? ResponseEntity.status(HttpStatus.OK).body("Item Deleted Successfully") :
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete Item");
    }

}
