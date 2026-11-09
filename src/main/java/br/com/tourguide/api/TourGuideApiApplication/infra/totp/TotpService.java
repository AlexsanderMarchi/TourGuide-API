package br.com.tourguide.api.TourGuideApiApplication.infra.totp;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import com.atlassian.onetime.service.RandomSecretProvider;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    public String gerarSecret(){
        return new RandomSecretProvider().generateSecret().getBase32Encoded();
    }

    public String gerarQrCode(UsuarioEntity usuario){
        // Otpauth URL: otpauth://totp/<Issuer>:<User>?secret=<Secret>&issuer=<Issuer>

        var issuer = "Tour Guide";
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, usuario.getUsername(), usuario.getSecret(), issuer
        );
    }
}
