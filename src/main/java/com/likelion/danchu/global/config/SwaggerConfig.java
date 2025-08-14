package com.likelion.danchu.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "🧵 단추 API 명세서",
            description =
                """
            <p>당신을 위한 <strong>단골 추억 서비스, 단추</strong>는<br>
            지역 가게를 중심으로 한 단골 문화와 고객의 경험을 연결하는 플랫폼입니다.</p>
            <ul>
                <li>테스트 시에는 <strong>테스트 계정</strong>으로 로그인됩니다.</li>
                <li><strong>AI 기반 추천</strong>을 통해 사용자의 관심사에 맞는 가게를 카드 형태로 제공합니다.</li>
                <li><strong>일일 미션</strong> 참여를 통해 자연스럽게 플랫폼에 대한 이용 경험을 확장할 수 있습니다.</li>
                <li><strong>오프라인 인증코드 입력</strong>만으로 종이 스탬프 없이 디지털 적립이 가능합니다.</li>
                <li>적립된 <strong>스탬프는 쿠폰으로 전환</strong>되며, 사용자 재방문을 유도할 수 있습니다.</li>
                <li><strong>해시태그 기반 필터</strong>를 활용해 원하는 미션이나 가게를 쉽게 탐색할 수 있습니다.</li>
                <li><strong>마이페이지</strong>에서 쿠폰함, 관심 해시태그, 프로필 정보 등을 편리하게 관리할 수 있습니다.</li>
                <li><strong>운영 서버와 로컬 환경</strong> 모두에서 Swagger UI를 통해 직접 API 테스트가 가능합니다.</li>
            </ul>
            <p><strong>⚠️ 테스트 방법 안내</strong></p>
            <ul>
                <li><strong>테스트 로그인 API</strong>를 통해 JWT를 발급받을 수 있습니다.</li>
                <li>발급된 JWT를 상단의 <strong>"Authorize"</strong> 버튼을 눌러 입력해야 인증이 필요한 API를 사용할 수 있습니다.</li>
                <li>JWT 토큰은 <strong>접두사 없이</strong> 토큰 값만 입력하면 Swagger가 자동으로 <strong>Bearer</strong>를 붙여줍니다.</li>
            </ul>
            """,
            contact =
                @Contact(name = "단추", url = "https://danchu.site", email = "1030n@naver.com")),
    security = @SecurityRequirement(name = "Authorization"),
    servers = {
      @Server(url = "https://api.danchu.site", description = "🚀 운영 서버"),
      @Server(url = "http://localhost:8080", description = "🛠️ 로컬 서버")
    })
@SecurityScheme(
    name = "Authorization",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT")
public class SwaggerConfig {

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("Swagger API") // API 그룹명
        .pathsToMatch("/api/**", "/swagger-ui/**", "/v3/api-docs/**")
        .build();
  }
}
