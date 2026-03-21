package cz.cvut.fel.ts.ts_semestralni_prace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String id;
    private String username;
    private String password;
    private String email;
    private String role; // ROLE_USER or ROLE_ADMIN
    private String firstName;
    private String lastName;
}
