package com.csit321g4.miljuanat.skillpulse.config;

import com.csit321g4.miljuanat.skillpulse.model.Admin;
import com.csit321g4.miljuanat.skillpulse.model.Role;
import com.csit321g4.miljuanat.skillpulse.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first Admin account on an empty database so that other accounts can be provisioned. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final BootstrapAdminProperties props;

    public DataInitializer(UserRepository users, PasswordEncoder encoder, BootstrapAdminProperties props) {
        this.users = users;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void run(String... args) {
        if (users.existsByRole(Role.ADMIN)) return;
        Admin admin = new Admin();
        admin.setFullName(props.fullName());
        admin.setEmail(props.email());
        admin.setPasswordHash(encoder.encode(props.password()));
        users.save(admin);
        log.warn("Bootstrap admin '{}' created. Change its password immediately.", props.email());
    }
}
