package br.com.julianajesus.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.julianajesus.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();
        if (servletPath.startsWith("/taks/")) {
            // pegar a autenticacao (usuario e senha)
            var authorization = request.getHeader("Authorization"); // pego os dados de autenticação

            var authEncoded = authorization.substring("Basic".length()).trim(); // separa o Basic dos dados
                                                                                // criptografados e retiro o espaço
                                                                                // entre eles

            byte[] authDecode = Base64.getDecoder().decode(authEncoded); // decodifico os dados que estao criptografados

            var authString = new String(authDecode); // pega a decoficação e transformo em strings

            String[] credentials = authString.split(":"); // separo o name e password que esta juntos
            String username = credentials[0];
            String password = credentials[1];
            // validar usuario

            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401, "Usuario sem autorização");
            } else {
                // validar senha
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (passwordVerify.verified) {
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401, "Senha incorreta");
                }
            }

        } else {
            filterChain.doFilter(request, response);
        }

    }

}
