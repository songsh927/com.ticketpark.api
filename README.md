
# Ticketpark API SERVER  

### 1. 프로젝트 소개
  
티켓 예매 서비스와 같은 짧은 시간에 대규모 트래픽을 핸들링하는 서비스에 대한 학습과 구현을 목표로 시작한 프로젝트입니다.  
프론트엔드는 [링크](https://github.com/songsh927/com.ticketpark.io)에 있습니다.  
  
### 2. 사용 기술  
   
#### Backend 
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"> ![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

#### Frontend  
![Vue.js](https://img.shields.io/badge/vuejs-%2335495e.svg?style=for-the-badge&logo=vuedotjs&logoColor=%234FC08D)

#### DevOps
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white) ![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white) ![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white) <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white">   
  
  
### 3. 아키텍쳐  
  
```bash
src
└── main
    └── java
        └── com.ticketpark.ticketpark
            ├── admin
            ├── common
            │   ├── auth
            │   │   └── JwtProvider.java
            │   ├── config
            │   │   ├── RedisConfig.java
            │   │   └── SecurityConfig.java
            │   ├── dto
            │   │   ├── DefaultRes.java
            │   │   ├── StatusCode.java
            │   │   └── TokenInfo.java
            │   ├── exception
            │   │   ├── ApiException.java
            │   │   ├── ApiExceptionAdvice.java
            │   │   └── ExceptionEnum.java
            │   ├── filter
            │   │   └── JwtCommonFilter.java
            │   └── redis
            │       ├── AuthRedisService.java
            │       └── QueueRedisService.java
            ├── member
            │   ├── controller
            │   │   └── MemberController.java
            │   ├── dto
            │   │   ├── GetMemberInfoDTO.java
            │   │   ├── JoinDTO.java
            │   │   ├── LoginDTO.java
            │   │   └── UpdateDTO.java
            │   ├── entity
            │   │   ├── MemberEntity.java
            │   │   └── MemberSecurityEntity.java
            │   ├── repository
            │   │   └── MemberRepository.java
            │   └── service
            │       ├── MemberSecurityService.java
            │       └── MemberService.java
            ├── rpa
            │   └── WaitingQueueScheduler.java
            ├── ticket
            │   ├── controller
            │   │   └── TicketController.java
            │   ├── dto
            │   │   ├── PageResponseDTO.java
            │   │   ├── ReservationForm.java
            │   │   ├── TicketDetailDTO.java
            │   │   └── TicketListPageDTO.java
            │   ├── entity
            │   │   ├── TicketDetailEntity.java
            │   │   ├── TicketMainEntity.java
            │   │   └── TicketReserveEntity.java
            │   ├── repository
            │   │   ├── TicketJpaRepository.java
            │   │   └── TicketRepository.java
            │   └── service
            │       ├── QueueService.java
            │       └── TicketService.java
            ├── web
            │   └── WebController.java
            └── TicketparkApplication.java
```