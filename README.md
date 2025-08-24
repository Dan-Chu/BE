#  🧶 단추
> LIKELION 13th DanGoa! <br>
25.07.21 - 25.08.26

<img width="3840" height="2160" alt="image" src="https://github.com/user-attachments/assets/ca2ea692-3948-4f20-b7aa-7a81712c2125" />
<img width="3840" height="2160" alt="image" src="https://github.com/user-attachments/assets/d7f41a74-7caf-4b97-a094-f9e6641f8375" />
<img width="3840" height="2160" alt="image" src="https://github.com/user-attachments/assets/1fbb892c-4b12-4787-ae31-166d83c0c07a" />
<img width="3840" height="2160" alt="image" src="https://github.com/user-attachments/assets/943377a7-d909-47b1-8301-7cf89319fc83" />


### 🔗 Link
https://www.danchu.site
<br><br>

### BE Contributors
|<img src="https://avatars.githubusercontent.com/u/162952415?v=4" width="150" height="150"/>|<img width="150" height="150" src="https://github.com/user-attachments/assets/c70091fc-1544-4a4b-92b1-79073426fdce" />|
|:-:|:-:|
|[@naooung](https://github.com/naooung) 김나경 |[@shinchaerin79](https://github.com/shinchaerin79) 신채린 |
<br>

### Stacks
#### Design (UI/UX) 
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![Styled Components](https://img.shields.io/badge/Styled%20Components-DB7093?style=for-the-badge&logo=styled-components&logoColor=white)
![Adobe Photoshop](https://img.shields.io/badge/adobe%20photoshop-%2331A8FF.svg?style=for-the-badge&logo=adobe%20photoshop&logoColor=white)

#### Environment
![Visual Studio Code](https://img.shields.io/badge/Visual%20Studio%20Code-0078d7.svg?style=for-the-badge&logo=visual-studio-code&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellij-idea&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

#### Frontend 
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-4B32C3?style=for-the-badge&logo=vite&logoColor=white)
![axios](https://img.shields.io/badge/axios-007ACC?style=for-the-badge&logo=axios&logoColor=white)
![Netlify](https://img.shields.io/badge/netlify-%23000000.svg?style=for-the-badge&logo=netlify&logoColor=#00C7B7)

#### Backend
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-4A5B6D?style=for-the-badge&logo=spring-security&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-5D8AA8?style=for-the-badge&logo=spring-data&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)
<img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
<img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">   
<img src="https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white">
<img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white">
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

#### Communication
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
<br><br>

### Directory Structure
```
src/main/java/com/likelion/danchu
├── domain                           # 도메인별 기능 모듈
│   ├── auth                         # 인증/인가
│   ├── coupon                       # 쿠폰 도메인
│   ├── hashtag                      # 해시태그 도메인
│   ├── menu                         # 메뉴 도메인
│   ├── mission                      # 미션 도메인
│   ├── openAI                       # OpenAI 사용 도메인
│   ├── stamp                        # 스탬프 도메인 
│   ├── store                        # 가게 도메인 
│   └── user                         # 사용자 도메인
│
├── global                           # 전역 설정 및 공통 모듈
│   ├── common                       # 공통 유틸/상수/헬퍼
│   ├── config                       # 스프링/외부 라이브러리 설정(@Configuration)
│   ├── exception                    # 전역 예외/에러코드/ExceptionHandler
│   ├── jwt                          # JWT 토큰 관련
│   ├── response                     # 공통 API 응답
│   └── security                     # Spring Security 설정 및 커스텀 필터/핸들러
│
├── infra                            # 외부 인프라 연동 어댑터 계층
│   ├── kakao                        # 카카오 API 설정
│   ├── openAI                       # OpenAI 설정
│   ├── redis                        # Redis 설정
│   └── s3                           # AWS S3 업로드/다운로드 등
│
└── DanchuApplication                # 스프링 부트 실행 클래스
```
1. 도메인 중심 설계
   
	•	domain 패키지를 인증/인가(auth), 쿠폰(coupon), 미션(mission), 가게(store), 사용자(user) 등 기능 단위로 구분하였습니다.   
	•	각 도메인은 Entity, Repository, Service, Controller를 독립적으로 관리하여 비즈니스 로직을 명확히 분리하였습니다.   

2. 전역(Global) 계층

	•	global 패키지에 공통 모듈을 분리 관리하였습니다.   
	•	config를 통해 스프링 및 외부 라이브러리 설정을 관리하였으며,   
	•	security 및 jwt 모듈을 통해 Spring Security 기반 인증/인가와 JWT 발급 및 검증을 처리하였습니다.   
	•	exception 모듈에서 전역 예외를 처리하고 에러 코드를 관리하였으며,   
	•	response 모듈을 통해 모든 API 응답을 일관된 포맷으로 제공하였습니다.   

3. 인프라(Infra) 계층

	•	infra 패키지를 두어 외부 서비스 연동을 관리하였습니다.   
	•	카카오 OAuth, OpenAI API, Redis, AWS S3 등을 독립적으로 모듈화하여 도메인 로직과 infra stucture를 분리하였습니다.   

4. 보안 및 표준화

	•	Spring Security와 JWT를 활용하여 인증 및 인가 절차를 표준화하였습니다.   
	•	전역 예외 처리(@RestControllerAdvice)를 적용하여 안정적인 에러 핸들링을 구현하였습니다.   
	•	모든 API 응답을 GlobalResponse 형식으로 통일하여 일관된 데이터 전달 방식을 유지하였습니다.   

5. 모니터링 시스템

	•	운영 환경의 안정성을 확보하기 위해 모니터링 시스템을 구축하였습니다.   
	•	Dozzle을 통해 실시간 로그를 확인할 수 있도록 하였으며,   
	•	Prometheus와 Grafana를 도입하여 서버 및 애플리케이션 메트릭을 수집 및 시각화하였습니다.    
<br><br>

### System Architecture
<img width="2500" alt="image" src="https://github.com/user-attachments/assets/df4120f6-9de4-457e-96e4-538beefe90db" />
<br><br>

### ERD
<img width="2722" height="1364" alt="image" src="https://github.com/user-attachments/assets/c525d786-afb2-4318-a1c2-33888968ac30" />
<br><br>

## 🎯 Git & Branch Convention
### Git Convention
- 🎉 Start: Start New Project [:tada]
- ✨ Feat: 새로운 기능을 추가 [:sparkles]
- 🐛 Fix: 버그 수정 [:bug]
- 🎨 Design: CSS 등 사용자 UI 디자인 변경 [:art]
- ♻️ Refactor: 코드 리팩토링 [:recycle]
- 🔧 Settings: Changing configuration files [:wrench]
- 🗃️ Comment: 필요한 주석 추가 및 변경 [:card_file_box]
- ➕ Dependency/Plugin: Add a dependency/plugin [:heavy_plus_sign]
- 📝 Docs: 문서 수정 [:memo]
- 🔀 Merge: Merge branches [:twisted_rightwards_arrows:]
- 🚀 Deploy: Deploying stuff [:rocket]
- 🚚 Rename: 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 [:truck]
- 🔥 Remove: 파일을 삭제하는 작업만 수행한 경우 [:fire]
- ⏪️ Revert: 전 버전으로 롤백 [:rewind]   
<br>

### Branch Convention
<img src="https://github.com/user-attachments/assets/b0e390be-6e1c-4bc0-85df-b1bba265716a" width="800">   

#### 기능 개발
  - develop 브랜치에서 feature/* 브랜치 생성
  - 개발 완료 후 → feature/* → develop 병합
#### 리팩토링
  - develop 브랜치에서 refactor/* 브랜치 생성
  - 수정 완료 후 → refactor/* → develop 병합
#### 배포
  - develop → main 병합 후 운영 배포
  - main 브랜치는 항상 운영 가능한 상태 유지

```
	이슈 생성 → 브랜치 생성 → PR 생성 → 코드 리뷰 & Approve → Merge 순서
 	⭐️ PR 및 Merge 과정을 통해 협업 효율성, 코드 품질, 이력 관리의 명확성을 확보
```
