
# FE 개발자를 위한 랭킹 페이지 수정 가이드

안녕하세요! 백엔드 랭킹 시스템이 새롭게 개편되었습니다. 아래 내용을 참고하여 랭킹 페이지를 수정해주세요.

## 주요 변경 사항

- **지역 및 기간 필터 제거**: 기존의 지역, 기간별 랭킹 조회가 사라지고, **전체 사용자 대상 통합 랭킹**으로 변경되었습니다.
- **API 엔드포인트 변경**: 랭킹 조회 및 '내 순위' 조회 API가 분리되고 단순화되었습니다.
- **실시간 데이터**: 모든 랭킹 데이터는 스냅샷 없이 실시간으로 제공됩니다.

---

## API 변경 상세 안내

### 1. 전체 랭킹 조회 API

전체 사용자의 랭킹 목록을 가져옵니다.

- **`GET /api/v1/ranking`**
- **인증**: 필요 (Authorization 헤더에 Bearer 토큰 포함)
- **요청 파라미터**: 없음
- **응답 데이터 형식**:

```json
{
  "topRankings": [
    {
      "rank": 1,
      "nickname": "에코워리어",
      "level": 5,
      "points": 4500
    },
    {
      "rank": 2,
      "nickname": "그린가디언",
      "level": 4,
      "points": 3800
    },
    // ... 다른 사용자 랭킹
  ]
}
```

- **`RankingItemDto` 상세**:
  - `rank` (number): 순위
  - `nickname` (string): 사용자 닉네임
  - `level` (number): 사용자 레벨
  - `points` (number): 사용자 보유 포인트

### 2. 내 랭킹 조회 API

현재 로그인된 사용자의 순위 정보를 가져옵니다.

- **`GET /api/v1/ranking/my-rank`**
- **인증**: 필요 (Authorization 헤더에 Bearer 토큰 포함)
- **요청 파라미터**: 없음
- **응답 데이터 형식**:

```json
{
  "rank": 15,
  "nickname": "나의닉네임",
  "level": 2,
  "points": 1200
}
```

- **`MyRankDto` 상세**:
  - `rank` (number): 내 순위
  - `nickname` (string): 내 닉네임
  - `level` (number): 내 레벨
  - `points` (number): 내 보유 포인트

---

## FE 수정 작업 요약

1.  **랭킹 페이지 UI 변경**:
    - '지역' 및 '기간'을 선택하는 드롭다운 또는 필터 UI를 **제거**합니다.
    - 이제 페이지는 항상 전체 사용자 랭킹을 표시합니다.

2.  **데이터 호출 로직 수정**:
    - 컴포넌트 마운트 시, `GET /api/v1/ranking` API를 호출하여 전체 랭킹 데이터를 가져와 목록에 표시합니다.
    - 동시에 `GET /api/v1/ranking/my-rank` API를 호출하여 현재 로그인한 사용자의 순위 정보를 가져와 '내 순위' 섹션에 표시합니다.

3.  **상태 관리 (예: React Query, SWR, Redux 등)**:
    - 기존 `useRankingQuery`와 같은 커스텀 훅이 있다면, 요청 파라미터(region, period)를 제거하고 새로운 API 엔드포인트에 맞게 수정합니다.
    - '전체 랭킹'과 '내 랭킹'을 위한 두 개의 개별적인 query key를 사용하는 것이 좋습니다.
      - 예: `queryKey: ['ranking']`
      - 예: `queryKey: ['myRank']`

---

## 코드 예시 (React, axios 기반)

아래는 수정된 로직의 간단한 예시입니다. 그대로 복사하여 사용하거나 프로젝트 구조에 맞게 수정하여 적용할 수 있습니다.

```jsx
import React, { useState, useEffect } from 'react';
import axios from 'axios'; // 또는 사용하는 HTTP 클라이언트

const RankingPage = () => {
  const [rankings, setRankings] = useState([]);
  const [myRank, setMyRank] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchRankingData = async () => {
      try {
        setLoading(true);
        
        // 두 API를 동시에 호출
        const [rankingResponse, myRankResponse] = await Promise.all([
          axios.get('/api/v1/ranking', {
            headers: {
              Authorization: `Bearer YOUR_AUTH_TOKEN`, // 실제 토큰으로 교체
            },
          }),
          axios.get('/api/v1/ranking/my-rank', {
            headers: {
              Authorization: `Bearer YOUR_AUTH_TOKEN`, // 실제 토큰으로 교체
            },
          }),
        ]);

        setRankings(rankingResponse.data.topRankings);
        setMyRank(myRankResponse.data);
        
      } catch (err) {
        setError('랭킹 정보를 불러오는 데 실패했습니다.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchRankingData();
  }, []);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div>
      <h1>전체 랭킹</h1>

      {/* 내 랭킹 정보 표시 */}
      {myRank && (
        <div style={{ border: '2px solid blue', padding: '10px', marginBottom: '20px' }}>
          <h2>내 순위</h2>
          <p><strong>순위: {myRank.rank}위</strong></p>
          <p>닉네임: {myRank.nickname}</p>
          <p>레벨: {myRank.level}</p>
          <p>포인트: {myRank.points}점</p>
        </div>
      )}

      {/* 전체 랭킹 목록 */}
      <table>
        <thead>
          <tr>
            <th>순위</th>
            <th>닉네임</th>
            <th>레벨</th>
            <th>포인트</th>
          </tr>
        </thead>
        <tbody>
          {rankings.map((user) => (
            <tr key={user.rank}>
              <td>{user.rank}</td>
              <td>{user.nickname}</td>
              <td>{user.level}</td>
              <td>{user.points}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default RankingPage;
```

---

## 백엔드 참고사항

- `src/main/java/app/greenpoint/domain/RankingSnapshot.java`
- `src/main/java/app/greenpoint/repository/RankingSnapshotRepository.java`

위 두 파일은 더 이상 사용되지 않으므로, 다음 백엔드 배포 시 삭제될 예정입니다.

궁금한 점이 있다면 언제든지 문의해주세요!
