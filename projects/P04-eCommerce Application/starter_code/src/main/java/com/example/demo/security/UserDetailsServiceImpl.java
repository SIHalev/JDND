package com.example.demo.security;

import static java.util.Collections.emptyList;

import com.example.demo.model.persistence.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

  private final UserRepository userRepository;

  @Autowired
  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(
      String username) throws UsernameNotFoundException {
    com.example.demo.model.persistence.User applicationUser = userRepository
        .findByUsername(username);
    if (applicationUser == null) {
      LOGGER.error("No application user available for {}", username);
      throw new UsernameNotFoundException(username);
    }

    LOGGER.debug("User successfully loaded {}", username);
    return new User(applicationUser.getUsername(), applicationUser.getPassword(), emptyList());
  }
}
