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

import foundIT.demo.model.admin;
import foundIT.demo.service.adminService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/admins")
public class adminController {
    

    private final adminService aService;


    public adminController(adminService aService)
    {
        this.aService = aService;
    }

    // Create - Post /api/admins
    @PostMapping
    public ResponseEntity<admin> createAdmin(@Valid @RequestBody admin a)
    {
        var savedAdmin = aService.createAdmin(a);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAdmin);
    }

    // Create - Post /api/admins/signin
    @PostMapping("/signin")
    public ResponseEntity<String> signInAdmin(@Valid @RequestBody admin a)
    {
        if(aService.passwordMatches(a))
        {
            return ResponseEntity.status(HttpStatus.OK).body("Sign in successful");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Sign in unSuccessful");
        //return ResponseEntity.status(HttpStatus.CREATED).body(savedAdmin);
    }

    // Read - Get /api/admins
    @GetMapping
    public ResponseEntity<List<admin>> getAllAdmins()
    {
        List<admin> adminList = aService.getAllAdmins();

        return ResponseEntity.status(HttpStatus.OK).body(adminList);
    }


    // Read - Get /api/admins/{id}
    @GetMapping("/{id}")
    public ResponseEntity<admin> updateAdmin(@PathVariable String id)
    {
        admin a = aService.getAdminById(id).orElse(null);
        return ResponseEntity.status(HttpStatus.OK).body(a);
    }
    
    // Update - put /api/admins/{id}
    @PutMapping("/{id}")
    public ResponseEntity<admin> updatedAdmin(@PathVariable String id,
                                                @RequestBody admin a)
    {
        if(!aService.getAdminById(id).isPresent())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var updatedAdmin = aService.updateAdmin(id, a);
        return ResponseEntity.status(HttpStatus.OK).body(updatedAdmin);
    }

    // Delete - Delete /api/admins/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAdmin(@PathVariable String id)
    {
        var deleted = aService.deleteAdmin(id);
        return deleted ? ResponseEntity.status(HttpStatus.OK).body("Admin Deleted Successfully") :
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unable to delete Admin");
    }

}
