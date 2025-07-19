package com.jammering.akkipy.runner;

import com.jammering.akkipy.domain.regions.Sido;
import com.jammering.akkipy.domain.regions.SidoRepository;
import com.jammering.akkipy.domain.regions.Sigungu;
import com.jammering.akkipy.domain.regions.SigunguRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class RegionDataLoader implements ApplicationRunner {
    private final SidoRepository sidoRepository;
    private final SigunguRepository sigunguRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource res = new ClassPathResource("sql/output.csv");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // 헤더 스킵
            Map<String, Sido> cache = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1);
                String rawSido   = cols[0].trim();
                String sigunguName = cols[1].trim();
                if (rawSido.isEmpty() || sigunguName.isEmpty()) continue;

                // 1) 접미사 제거 + 2) 약어 매핑
                String sidoName = normalizeSido(rawSido);

                // 캐시에서 꺼내거나 DB 에 없으면 저장
                Sido sido = cache.computeIfAbsent(sidoName, name ->
                        sidoRepository.findByName(name)
                                .orElseGet(() -> sidoRepository.save(new Sido(name)))
                );

                Sigungu sg = new Sigungu();
                sg.setName(sigunguName);
                sg.setSido(sido);
                sigunguRepository.save(sg);
            }
        }
    }

    /**
     * 1) "특별자치시","특별시","광역시","특별자치도","도" 등 접미사 자르고
     * 2) 전라남→전남, 충청북→충북 같은 약어로 바꿔줌
     */
    private String normalizeSido(String raw) {
        String name = raw;
        // 1) 접미사 제거 (길이 순)
        for (String suffix : List.of("특별자치시","특별자치도","광역시","특별시","도")) {
            if (name.endsWith(suffix)) {
                name = name.substring(0, name.length() - suffix.length());
                break;
            }
        }
        // 2) 약어 매핑
        Map<String, String> abbr = Map.ofEntries(
                Map.entry("전라남", "전남"),
                Map.entry("전라북", "전북"),
                Map.entry("충청남", "충남"),
                Map.entry("충청북", "충북"),
                Map.entry("경상남", "경남"),
                Map.entry("경상북", "경북"),
                Map.entry("경기",   "경기"),
                Map.entry("강원",   "강원"),
                Map.entry("제주",   "제주"),
                Map.entry("세종",   "세종"),
                Map.entry("서울",   "서울"),
                Map.entry("부산",   "부산"),
                Map.entry("대구",   "대구"),
                Map.entry("인천",   "인천"),
                Map.entry("광주",   "광주"),
                Map.entry("대전",   "대전"),
                Map.entry("울산",   "울산")
        );
        return abbr.getOrDefault(name, name);
    }
}