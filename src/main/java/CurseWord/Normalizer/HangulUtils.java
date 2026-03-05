package CurseWord.Normalizer;

import org.springframework.stereotype.Component;

@Component
public class HangulUtils {
    // 한글 초성 리스트 (유니코드 순서)
    private final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    /**
     * 문자열에서 한글 완성형 글자를 초성으로 변환합니다.
     * 한글이 아니거나 이미 초성인 경우는 그대로 유지합니다.
     */
    public String getChosung(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c >= 0xAC00 && c <= 0xD7A3) { // 완성형 한글 범위
                int base = c - 0xAC00;
                int chosungIndex = base / (21 * 28);
                sb.append(CHOSUNG[chosungIndex]);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}