package io.growbymastery.ppmtool.web;

import io.growbymastery.ppmtool.domain.User;
import io.growbymastery.ppmtool.payload.JWTLoginSuccessResponse;
import io.growbymastery.ppmtool.payload.JWTTokenProvider;
import io.growbymastery.ppmtool.payload.LoginRequest;
import io.growbymastery.ppmtool.services.MapValidationErrorService;
import io.growbymastery.ppmtool.services.UserService;
import io.growbymastery.ppmtool.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static io.growbymastery.ppmtool.security.SecurityConstants.TOKEN_PREFIX;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired MapValidationErrorService mapValidationErrorService;

  @Autowired UserService userService;

  @Autowired private UserValidator userValidator;

  @Autowired private JWTTokenProvider jwtTokenProvider;

  @Autowired private AuthenticationManager authenticationManager;

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(
      @Valid @RequestBody LoginRequest loginRequest, BindingResult result) {
    ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationErrorService(result);
    if (errorMap != null) return errorMap;

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = TOKEN_PREFIX + jwtTokenProvider.generateToken(authentication);

    return ResponseEntity.ok(new JWTLoginSuccessResponse(true, jwt));
  }

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@Valid @RequestBody User user, BindingResult result) {
    // validate password match
    userValidator.validate(user, result);

    ResponseEntity<?> errorMap = mapValidationErrorService.MapValidationErrorService(result);
    if (errorMap != null) return errorMap;

    User newUser = userService.saveUser(user);

    return new ResponseEntity<>(newUser, HttpStatus.CREATED);
  }
}
