package foundIT.demo.service;


import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import foundIT.demo.model.admin;

@Service
public class adminService
{
    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;


    /** Wires Mongo persistence and password hashing for admin records. */
    public adminService(MongoTemplate mongoTemplate,  PasswordEncoder passwordEncoder)
    {
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    /** Saves a new admin after rejecting duplicate usernames and hashing the password. */
    public admin createAdmin(admin ad)
    {
        if(usernameExists(ad))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose another Username");
        }

        String encodedPassword = passwordEncoder.encode(ad.getPassword());
        ad.setPassword(encodedPassword);

        return mongoTemplate.save(ad);
    }

    /** Returns every admin record from the database. */
    public List<admin> getAllAdmins()
    {
        return mongoTemplate.findAll(admin.class);
    }

    /** Looks up an admin by Mongo id. */
    public Optional<admin> getAdminById(String id)
    {
        var admin = mongoTemplate.findById(id, admin.class);

        return Optional.ofNullable(admin);
    }

    /** Finds a single admin by username. */
    public Optional<admin> getAdminByUsername(String username)
    {
        var query = new Query(Criteria.where("username").is(username));
        var admin = mongoTemplate.findOne(query, admin.class);

        return Optional.ofNullable(admin);
    }

    /** Authenticates a sign-in attempt by username and password. */
    public Optional<admin> authenticate(String username, String password)
    {
        return getAdminByUsername(username)
                .filter(existingAdmin -> passwordMatches(password, existingAdmin.getPassword()));
    }

    /** Updates the admin profile fields for the requested record. */
    public admin updateAdmin(String id, admin admin)
    {
        if(usernameExists(admin))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose another Username");
        }
        var query = new Query(Criteria.where("id").is(id));

        Update updatedAdmin = new Update()
                        .set("username", admin.getUsername())
                        .set("password", admin.getPassword())
                        .set("firstName", admin.getFirstName())
                        .set("lastName", admin.getLastName());        

        mongoTemplate.updateFirst(query, updatedAdmin, admin.class);

        return getAdminById(id).orElse(null);
    }


    /** Deletes an admin record by id and reports whether anything was removed. */
    public boolean deleteAdmin(String id)
    {
        var query = new Query(Criteria.where("id").is(id));

        return mongoTemplate.remove(query, admin.class).getDeletedCount() > 0;
    }


    /** Checks whether the requested username is already present in the admin collection. */
    public boolean usernameExists(admin ia)
    {
        List<admin> la = getAllAdmins();
        for ( admin a : la)
        {
            if (a.getUsername().equals(ia.getUsername()))
            {
                return true;
            }
        }
        return false;
    }

    /** Returns the stored password string for the given username, if one exists. */
    public String findPassword(String user)
    {

        
        for ( admin a : getAllAdmins())
        {
            if (a.getUsername().equals(user))
            {
                return a.getPassword();
            }
        }

        return null;

    }

    /**
     * Validates an admin sign-in object by finding the stored password for the
     * username and then comparing plain text against either a bcrypt hash or a
     * previously stored raw value.
     */
    public boolean passwordMatches(admin a)
    {
        String rawPassword = a.getPassword();
        
        if (rawPassword == null || !usernameExists(a))
        {
            return false;
        }
        String storedPassword = findPassword(a.getUsername());

        return passwordEncoder.matches(rawPassword, storedPassword) || rawPassword.equals(storedPassword);
    }

    /** Compares a submitted password to the stored value while tolerating legacy raw passwords. */
    private boolean passwordMatches(String rawPassword, String storedPassword)
    {
        
        if (rawPassword == null || storedPassword == null || storedPassword.isBlank())
        {
            return false;
        }

        return passwordEncoder.matches(rawPassword, storedPassword) || rawPassword.equals(storedPassword);
    }




    
}
