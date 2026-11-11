package br.com.tourguide.api.TourGuideApiApplication.infra.totp;

import br.com.tourguide.api.TourGuideApiApplication.domain.usuario.UsuarioEntity;
import com.atlassian.onetime.core.TOTPGenerator;
import com.atlassian.onetime.model.TOTPSecret;
import com.atlassian.onetime.service.RandomSecretProvider;
import org.springframework.stereotype.Service;

/**
 * Service para lidar com a autnticação de dois fatores (A2F), tendo metódo para:
 * <ul>
 *     <li>Gerar secret</li>
 *     <li>Gerar QR Code</li>
 *     <li>Verificar código de 6 dígitos gerado pelo app autenticador</li>
 * </ul>
 *
 */
@Service
public class TotpService {

    /**
     * metodo para criar o secret que vai ser usada para criar p QR Code e para verificar depois com o código
     */
    public String gerarSecret() {
        return new RandomSecretProvider().generateSecret().getBase32Encoded();
    }

    /**
     * metodo que retorna a url do QR Code no formato "otpauth://totp/{Issuer}:{User}?secret={Secret}&issuer={Issuer}"
     */
    public String gerarQrCode(UsuarioEntity usuario) {
        String issuer = "Tour Guide";
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer, usuario.getUsername(), usuario.getSecret(), issuer
        );
    }

    /**
     * metodo que verifica o codigo autenticador de 6 digito com a secret
     */
    public Boolean verificarCodigo(String codigo, UsuarioEntity logado) {
        TOTPSecret secretDecodificada = TOTPSecret.Companion.fromBase32EncodedString(logado.getSecret());
        String codigoAplicacao = new TOTPGenerator().generateCurrent(secretDecodificada).getValue();

        return codigoAplicacao.equals(codigo);
    }
}
