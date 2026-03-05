package CurseWord.Normalizer;

import org.springframework.stereotype.Component;

@Component
public class TextNormalizer {

    //특수문자, 숫자 등을 제거하고 연속된 중복 문자를 하나로 압축합니다.
    public String normalize(String text) {
        if (text == null) return "";
        // 숫자, 특수문자 제거 및 중복 문자 압축
        String t = text.replaceAll("[^\\p{L}\\p{InHangul_Jamo}\\p{InHangul_Compatibility_Jamo}]", "");
        return t.replaceAll("(\\p{L})\\1+", "$1");
    }
}
