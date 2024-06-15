package com.product.ProductDetails.Controller;

import com.product.ProductDetails.Dto.AuthRequestDTO;
import com.product.ProductDetails.Dto.JwtResponseDTO;
import com.product.ProductDetails.Service.JwtService;
import com.product.ProductDetails.domain.User;
import com.product.ProductDetails.repository.UserRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class Authcontroller {

    @Autowired
    JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public Authcontroller(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/new")
    public ResponseEntity<?> create(@RequestBody User user){
        String userName = user.getUserName();
        Optional<User> checkUserName = userRepository.findByUserName(userName);
        if(checkUserName.isPresent()){
            return ResponseEntity.status(400).body("Email already exist");
        }
        user.setPassword(encoder.encode(user.getPassword()));
        User save = userRepository.save(user);
        return ResponseEntity.ok(save.getUserId());
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "http://localhost:3000")
    public JwtResponseDTO AuthenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        System.out.println("---- 0 ---"+" "+ authRequestDTO.getUsername() +" "+ authRequestDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
        System.out.println("----");
        if(authentication.isAuthenticated()){
            String id= authRequestDTO.getUsername();
            Optional<User> name=userRepository.findByUserName(id);
            if(name.isPresent()) {
                return JwtResponseDTO.builder()
                        .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername()))
                        .userId(name.get().getUserId())
                        .build();
            } } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }
        return null;
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/ping")

    public String test() {
        System.out.println("enter");
        try {
            System.out.println("welcome");
            return "Welcome";
        } catch (Exception e){
            System.out.println("exit");
            throw new RuntimeException(e);
        }
    }
}
