#  🧶 단추
가게마다 찍히는 단골의 추억을 연결하는 지역 기반 단골 플랫폼

<img width="300" height="300" alt="image" src="https://github.com/user-attachments/assets/9db6a458-694a-4090-8890-7193756c91cb" />
<br><br>

### Description
🤖 AI 추천: 관심사에 맞는 가게와 미션 카드 추천

🎯 일일 미션: 자연스러운 플랫폼 이용 경험 확장

🧾 디지털 적립: 인증코드 입력만으로 스탬프 적립 & 쿠폰 전환

🔍 해시태그 필터: 원하는 미션·가게 쉽게 탐색

🙋 마이페이지: 쿠폰함, 관심 해시태그, 프로필 관리
<br><br>

### Contributors
|<img src="https://avatars.githubusercontent.com/u/162952415?v=4" width="200" height="200"/>|<img width="200" height="200" src="https://github.com/user-attachments/assets/c70091fc-1544-4a4b-92b1-79073426fdce" />|
|:-:|:-:|
|[@naooung](https://github.com/naooung) 김나경 |[@shinchaerin79](https://github.com/shinchaerin79) 신채린 |
<br>

### Stacks
#### Design (UI/UX) 
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)  ![Styled Components](https://img.shields.io/badge/Styled%20Components-DB7093?style=for-the-badge&logo=styled-components&logoColor=white) ![Adobe Photoshop](https://img.shields.io/badge/adobe%20photoshop-%2331A8FF.svg?style=for-the-badge&logo=adobe%20photoshop&logoColor=white)

#### Environment
![Visual Studio Code](https://img.shields.io/badge/Visual%20Studio%20Code-0078d7.svg?style=for-the-badge&logo=visual-studio-code&logoColor=white)  ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellij-idea&logoColor=white)  ![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)    ![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

#### Frontend 
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)  ![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)   ![Vite](https://img.shields.io/badge/Vite-4B32C3?style=for-the-badge&logo=vite&logoColor=white) ![axios](https://img.shields.io/badge/axios-007ACC?style=for-the-badge&logo=axios&logoColor=white)   ![Netlify](https://img.shields.io/badge/netlify-%23000000.svg?style=for-the-badge&logo=netlify&logoColor=#00C7B7)

#### Backend
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)  ![Spring Security](https://img.shields.io/badge/Spring%20Security-4A5B6D?style=for-the-badge&logo=spring-security&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-5D8AA8?style=for-the-badge&logo=spring-data&logoColor=white)   ![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white) <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white">
<img src="https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white">  <img src="https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white">  ![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)   ![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=for-the-badge&logo=ubuntu&logoColor=white)

#### Communication
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)  ![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
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

### System Architecture
<img width="2500" alt="image" src="https://github.com/user-attachments/assets/cc7102c7-a95f-4a1a-a207-b67343c3d595" />
<br><br>

### ERD
<img width="2722" height="1364" alt="image" src="https://github.com/user-attachments/assets/c525d786-afb2-4318-a1c2-33888968ac30" />
<br><br>

# 🎯 Git & Branch Convention
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


### Branch Convention
<img src="https://github.com/user-attachments/assets/b0e390be-6e1c-4bc0-85df-b1bba265716a" width="800">
<br><br>

> 1. 기능 개발
>  - develop 브랜치에서 feature/* 브랜치 생성
>  - 개발 완료 후 → feature/* → develop 병합
> 2. 리팩토링
>  - develop 브랜치에서 refactor/* 브랜치 생성
>  - 수정 완료 후 → refactor/* → develop 병합
> 3. 배포
>  - develop → main 병합 후 운영 배포
>  - main 브랜치는 항상 운영 가능한 상태 유지
