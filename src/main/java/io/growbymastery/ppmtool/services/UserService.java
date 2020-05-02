package io.growbymastery.ppmtool.services;

import io.growbymastery.ppmtool.domain.User;
import io.growbymastery.ppmtool.exceptions.UsernameAlreadyExistException;
import io.growbymastery.ppmtool.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  @Autowired private BCryptPasswordEncoder bCryptPasswordEncoder;

  public User saveUser(User newUser) {
    try {
      newUser.setPassword(bCryptPasswordEncoder.encode(newUser.getPassword()));
      newUser.setUsername(newUser.getUsername());
      newUser.setConfirmPassword("");

      return userRepository.save(newUser);
    } catch (Exception e) {
      throw new UsernameAlreadyExistException(
          "Username " + newUser.getUsername() + " already exists !");
    }
  }
}
