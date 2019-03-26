package com.codeoftheweb.salvo;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

@SpringBootApplication
public class SalvoApplication {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(SalvoApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository, ShipRepository shipRepository, SalvoRepository salvoRepository, ScoreRepository scoreRepository) {
        return (args) -> {

            //save a couple of players
//            Player p1 = new Player(7,"test ", "1@1.com", passwordEncoder.encode("123"));
//            Player p2 = new Player(3,"YouGotPwnd ", "pwnd.example@mail.com", passwordEncoder.encode("passwordNTEST1"));
//            Player p3 = new Player(4,"BoatyMacBoat ", "boat.boaty@mail.com", passwordEncoder.encode("passwordN9807"));
//            Player p4 = new Player(5,"ImBatman ", "2@2.com", passwordEncoder.encode("123"));
//
//
//            //save a couple of games
//            Game g1 = new Game();
//            Game g2 = new Game();
//            g2.setCreationDate(g2.getCreationDate().plusSeconds(3600));
//            Game g3 = new Game();
//            g3.setCreationDate(g3.getCreationDate().plusSeconds(7200));
//
//
//            //save a couple of gameplayer
//            GamePlayer gp1 = new GamePlayer(g1, p1);
//            GamePlayer gp2 = new GamePlayer(g2, p4);
//            GamePlayer gp3 = new GamePlayer(g3, p3);
//            GamePlayer gp4 = new GamePlayer(g1, p4);
//            GamePlayer gp5 = new GamePlayer(g2, p1);
////            GamePlayer gp6 = new GamePlayer(g3, p2);
//
//
//            //save a couple of ships
//            List<String> shipLocationArray = Arrays.asList("A3", "A4", "A5","A6","A7");
//            Ship carrier = new Ship("carrier", shipLocationArray);
//            Ship patrolboat = new Ship("patrolboat",(Arrays.asList("B10", "C10")));
//            Ship destroyer = new Ship("destroyer",(Arrays.asList("H2", "H3", "H4")));
//            Ship battleship = new Ship("battleship", (Arrays.asList("E3", "E4", "E5", "E6")));
//            Ship submarine = new Ship("submarine",(Arrays.asList("G9","H9","I9")));
//
//            Ship carrier1 = new Ship("carrier", shipLocationArray);
//            Ship patrolboat1 = new Ship("patrolboat", (Arrays.asList("E5", "E6")));
//            Ship destroyer1 = new Ship("destroyer",(Arrays.asList("H2", "H3", "H4")));
//            Ship battleship1 = new Ship("battleship",(Arrays.asList("C6", "C7", "C8", "C9")));
//            Ship submarine1 = new Ship("submarine",(Arrays.asList("F1","F2","F3")));
//
//            Ship carrier2 = new Ship("carrier", (Arrays.asList("B2", "B3", "B4","B5","B6")));
//            Ship patrolboat2 = new Ship("patrolboat",(Arrays.asList("D5", "E5")));
//            Ship destroyer2 = new Ship("destroyer", (Arrays.asList("I5", "I6", "I7")));
//            Ship battleship2 = new Ship("battleship", (Arrays.asList("F9", "G9", "H9", "I9")));
//            Ship submarine2 = new Ship("submarine",(Arrays.asList("D1","D2","D3")));
//
//            gp1.addShip(carrier);
//            gp1.addShip(patrolboat);
//            gp1.addShip(destroyer);
//            gp1.addShip(battleship);
//            gp1.addShip(submarine);
//
//            gp2.addShip(carrier1);
//            gp2.addShip(patrolboat1);
//            gp2.addShip(destroyer1);
//            gp2.addShip(battleship1);
//            gp2.addShip(submarine1);
//
//            gp4.addShip(carrier2);
//            gp4.addShip(patrolboat2);
//            gp4.addShip(destroyer2);
//            gp4.addShip(battleship2);
//            gp4.addShip(submarine2);
//
//            //Save salvoes
//            Salvo salvoG1T1 = new Salvo(gp4, 1, new ArrayList<>(Arrays.asList("B5", "F5" ,"C6","D7","E4")));
//            Salvo salvoG2T1 = new Salvo(gp1, 1, new ArrayList<>(Arrays.asList("C2", "D8","D4","A1","B5")));
////            Salvo salvoG1T2 = new Salvo(gp1, 2, new ArrayList<>(Arrays.asList("A8", "H2","F4","D6","H1")));
////            Salvo salvoG2T2 = new Salvo(gp4, 2, new ArrayList<>(Arrays.asList("H2", "A8","B9","B8","C1")));
////            Salvo salvoG1T3 = new Salvo(gp1, 3, new ArrayList<>(Arrays.asList("F9", "E5","F1","H5","A5")));
////            Salvo salvoG2T3 = new Salvo(gp4, 3, new ArrayList<>(Arrays.asList("C10", "I2","F8","F4","E1")));
//
////            gp1.addSalvo(salvoG1T1);
////            gp1.addSalvo(salvoG1T2);
////            gp1.addSalvo(salvoG1T3);
////            gp2.addSalvo(salvoG2T1);
////            gp2.addSalvo(salvoG2T2);
////            gp2.addSalvo(salvoG2T3);
//
//            //Save Scores
//            Score scoreG1P1 = new Score(g1, p1, 1.0);
//            Score scoreG1P2 = new Score(g1, p4, 0.0);
//            Score scoreG2P1 = new Score(g2, p4, 0.5);
//            Score scoreG2P2 = new Score(g2, p1, 0.5);
////            Score scoreG3P1 = new Score (g3,p3,0.0);
////            Score scoreG3P2 = new Score (g3,p2,0.0);
//
//
//
//            playerRepository.save(p1);
//            playerRepository.save(p2);
//            playerRepository.save(p3);
//            playerRepository.save(p4);
//
//            gameRepository.save(g1);
//            gameRepository.save(g2);
//            gameRepository.save(g3);
//
//            gamePlayerRepository.save(gp1);
//            gamePlayerRepository.save(gp2);
//            gamePlayerRepository.save(gp3);
//            gamePlayerRepository.save(gp4);
//            gamePlayerRepository.save(gp5);
////            gamePlayerRepository.save(gp6);
//
//            salvoRepository.save(salvoG1T1);
//            salvoRepository.save(salvoG2T1);
////            salvoRepository.save(salvoG1T2);
////            salvoRepository.save(salvoG2T2);
////            salvoRepository.save(salvoG1T3);
////            salvoRepository.save(salvoG2T3);
//
//            shipRepository.save(carrier);
//            shipRepository.save(patrolboat);
//            shipRepository.save(destroyer);
//            shipRepository.save(battleship);
//            shipRepository.save(submarine);
//
//            shipRepository.save(carrier1);
//            shipRepository.save(patrolboat1);
//            shipRepository.save(destroyer1);
//            shipRepository.save(battleship1);
//            shipRepository.save(submarine1);
//
//            shipRepository.save(carrier2);
//            shipRepository.save(patrolboat2);
//            shipRepository.save(destroyer2);
//            shipRepository.save(battleship2);
//            shipRepository.save(submarine2);
////
//            scoreRepository.save(scoreG1P1);
//            scoreRepository.save(scoreG1P2);
//            scoreRepository.save(scoreG2P1);
//            scoreRepository.save(scoreG2P2);
//            scoreRepository.save(scoreG3P1);
//            scoreRepository.save(scoreG3P2);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(inputName -> {
            System.out.println("inputName: " + inputName);
            Player player = playerRepository.findByEmail(inputName);
            System.out.println("Player: " + player);
            if (player != null) {
                System.out.println("Player in database...");
                return new User(player.getEmail(), player.getPassword(),
                        AuthorityUtils.createAuthorityList("USER"));
            } else {
                System.out.println("Failed... Unknow user: "+inputName);
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
        // turn off checking for CSRF tokens
        http.requiresChannel()
                .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                .requiresSecure();

        http
                .cors()
                .and()
                .csrf().disable()
                .authorizeRequests()
//                .antMatchers("/admin/**").hasAuthority("ADMIN")
                .antMatchers("/api/games").permitAll()
                .antMatchers("/api/players").permitAll()
                .anyRequest().fullyAuthenticated();
//                .anyRequest().permitAll();
//                .and()
//                .formLogin();

        http.formLogin()
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        http.headers().frameOptions().disable();
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedOrigins(Arrays.asList("https://battleship-battlewreck-game.netlify.com"));
        configuration.setAllowedMethods(Arrays.asList("HEAD",
                "GET", "POST", "PUT", "DELETE", "PATCH"));
        // setAllowCredentials(true) is important, otherwise:
        // will fail with 403 Invalid CORS request
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}