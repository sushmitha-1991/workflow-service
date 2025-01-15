/**
 * 
 * @author Mrinal Lakhanpal
 * @version 1.0
 */
package com.dsp.workflow.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "authentication") // Updated table name to match entity
public class BaseUser extends Auditable implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_user_seq")
	@SequenceGenerator(name = "my_user_seq", sequenceName = "my_user_seq", allocationSize = 1)
	private Long id;

	@Column(name = "username", unique = true, nullable = true)
	private String username;

	@Column(name = "phone_number", unique = true, nullable = false)
	@Size(min = 10, max = 10, message = "Phone number must be 10 digits")
	private String phone;

	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "role", nullable = true)
	private String role;

	// Merged User entity properties

	@Column(name = "first_name", nullable = false, length = 50)
	private String firstName;

	@Column(name = "middle_name", length = 50)
	private String middleName;

	@Column(name = "last_name", nullable = false, length = 50)
	private String lastName;

//    @Column(name = "password_hash", nullable = false, length = 255)
//    private String passwordHash;

	@Column(name = "profile_picture", length = 255)
	private String profilePicture; // Added profilePicture field

	@Column(name = "address", length = 255)
	private String address;

	@Column(name = "is_verified")
	private Boolean isVerified = false;

	@Column(name = "house_id", nullable = true)
	private Long houseId;

	// Implement UserDetails interface methods

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Here, you should return the roles and authorities granted to the user.
		// If you're not using GrantedAuthority, you can return an empty list or
		// customize as needed.
		return Collections.emptyList();
	}

	@Override
	public boolean isAccountNonExpired() {
		// Return true if the account is still valid (non-expired)
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// Return true if the account is not locked
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// Return true if the credentials are valid (non-expired)
		return true;
	}

//    @Override
//    public boolean isEnabled() {
//        // Return true if the user is enabled
//        return this.isActive;
//    }

	// Ensuring the method exists for AuditorAware
	public Long getUserId() {
		return this.id;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}
}
