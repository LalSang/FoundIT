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


    public itemController(itemService iService)
    {
        this.iService = iService;
    }

    // Create - Post /api/items
    @PostMapping
    public ResponseEntity<item> createItem(@Valid @RequestBody item i)
    {
        var savedItem = iService.createItem(i);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
    }

    // Read - Get /api/items
    @GetMapping
    public ResponseEntity<List<item>> getAllItems()
    {
        List<item> itemList = iService.getAllItems();

        return ResponseEntity.status(HttpStatus.OK).body(itemList);
    }

    // Read - Get /api/items/{id}
    @GetMapping("/{id}")
    public ResponseEntity<item> updateItem(@PathVariable String id)
    {
        item i = iService.getItemById(id).orElse(null);
        return ResponseEntity.status(HttpStatus.OK).body(i);
    }
    
    // Update - put /api/items/{id}
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

    // Delete - Delete /api/items/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable String id)
    {
        var deleted = iService.deleteItem(id);
        return deleted ? ResponseEntity.status(HttpStatus.OK).body("Item Deleted Successfully") :
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete Item");
    }

}
