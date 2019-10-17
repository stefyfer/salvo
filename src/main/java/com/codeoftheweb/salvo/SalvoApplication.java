package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;

@SpringBootApplication
public class SalvoApplication {

	public static void main(String[] args) {

		SpringApplication.run(SalvoApplication.class, args);
		System.out.println("OK Stefi");
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository,
									  GamePlayerRepository gamePlayerRepository,
									  GameRepository gameRepository,
									  ShipRepository shipRepository,
									  SalvoRepository salvoRepository,
                                      ScoreRepository scoreRepository) {

		return (args) -> {
			// save a couple of customers
			Player p1= new Player("j.bauer@ctu.gov", passwordEncoder().encode("1234"));
			playerRepository.save(p1);
			Player p2= new Player("c.obrian@ctu.gov", passwordEncoder().encode("1234"));
			playerRepository.save(p2);
			Player p3= new Player("kim_bauer@gmail.gov", passwordEncoder().encode("1234"));
			playerRepository.save(p3);
			Player p4= new Player("t.almeida@ctu.gov", passwordEncoder().encode("1234"));
			playerRepository.save(p4);
			Player p5= new Player("jbcrodriguezsud@gmail.com", passwordEncoder().encode("1234"));
			playerRepository.save(p5);

            Player p6= new Player("david@gmail.com", passwordEncoder().encode("1234"));
            playerRepository.save(p6);

			Date date1 = new Date();
			Game g1 = new Game(date1);
			gameRepository.save(g1);

			Date date2 = Date.from(date1.toInstant().plusSeconds(3600));
			Game g2 = new Game(date1);
			gameRepository.save(g2);

			Date date3 = Date.from(date2.toInstant().plusSeconds(3600));
			Game g3 = new Game(date3);
			gameRepository.save(g3);

			GamePlayer gp1 =new GamePlayer(new Date(),g1, p1);
			GamePlayer gp2 =new GamePlayer(new Date(),g1, p2);
			//
			GamePlayer gp3 =new GamePlayer(date2,g2, p3);
			GamePlayer gp4 =new GamePlayer(date2,g2, p4);
			//
			GamePlayer gp5 =new GamePlayer(date3,g3, p5);
			GamePlayer gp6 =new GamePlayer(date3,g3, p6);

			gamePlayerRepository.save(gp1);
			gamePlayerRepository.save(gp2);
			gamePlayerRepository.save(gp3);
			gamePlayerRepository.save(gp4);
			gamePlayerRepository.save(gp5);
			gamePlayerRepository.save(gp6);

			String battleship = "Battleship";
			String carrier = "Carrier";
			String submarine = "Submarine";
			String destroyer = "Destroyer";
			String patrolBoat = "Patrol Boat";
			Ship ship1 = new Ship(destroyer, Arrays.asList("H2", "H3", "H4"),gp1);
			Ship ship2 = new Ship(submarine, Arrays.asList("E1", "F1", "G1"),gp1);
			Ship ship3 = new Ship(patrolBoat, Arrays.asList("B4", "B5"),gp2);
			Ship ship4 = new Ship(destroyer, Arrays.asList("B5", "C5", "D5"),gp2);
			Ship ship5 = new Ship(patrolBoat, Arrays.asList("F1", "F2"),gp1);

			shipRepository.save(ship1);
			shipRepository.save(ship2);
			shipRepository.save(ship3);
			shipRepository.save(ship4);
			shipRepository.save(ship5);

			Salvo salvo1 = new Salvo(1, Arrays.asList("H2","H3","H4"), gp1);
			Salvo salvo2 = new Salvo(1, Arrays.asList("E1", "F1", "G1"), gp2);

			salvoRepository.save(salvo1);
			salvoRepository.save(salvo2);


            Score score1 = new Score(p1,g1,1.0D,new Date());
            Score score2 = new Score(p2,g1,0.0D,new Date());

            scoreRepository.save(score1);
            scoreRepository.save(score2);


        };
	}
}
@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = playerRepository.findByUserName(inputName);
			if (player != null) {
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}

}
@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/web/games_2.html").permitAll()
                .antMatchers("/api/game_view/*").hasAuthority("USER")
                .antMatchers("/web/**").permitAll()
                .antMatchers("/api/games").permitAll()
                .antMatchers("/api/players").permitAll()
                .antMatchers("/rest").denyAll()
                .anyRequest().permitAll();
        http.formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }
    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }

}

