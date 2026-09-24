# 功能完整性与答辩可信度 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让当前高校数据分析平台的核心页面、接口、测试和项目文档真实一致，形成可运行、可验证、可解释的毕业设计答辩闭环。

**Architecture:** 保留现有 Vue 3 + Spring Boot + MySQL + Python 分层，不引入 Redis、EasyExcel、PDF 或新的任务调度系统。后端负责统一接口、权限、校验和预测数据来源；前端负责明确区分加载中、真实数据、空数据、错误和演示数据；文档只陈述已经验证的能力。

**Tech Stack:** Java 17, Spring Boot 3.2.5, Spring Security, JWT, MyBatis-Plus, MySQL 8, Vue 3, TypeScript, Vite, ECharts, Python 3.11, Pandas/NumPy/Scikit-learn.

**Spec:** `docs/superpowers/specs/2026-09-24-function-completeness-design.md`

## Global Constraints

- 本阶段不扩大为新的平台建设，不引入 Redis、EasyExcel 或 PDF 生成等大组件。
- 不破坏当前工作区已有未提交改动，不回滚用户现有截图、主题和页面调整。
- 默认情况下，请求失败不再静默切换为看似真实的模拟数据。
- 未包含的功能保留为后续扩展，并从“已完成能力”描述中移除或标注为规划项。
- 需要数据库的测试使用当前本地测试数据库或已有测试配置，不进行真实生产数据写入。
- 不执行 Git commit；每个任务结束时只检查 `git diff` 和验证结果，保留用户后续审阅和提交权。

## Review Focus

- 预测表为空或缺少某个预测类型时，接口必须返回可解释的空值/状态，不能抛出空指针或填入伪造数字；由任务 1 的服务测试覆盖。
- 真实接口失败时，前端必须展示错误/重试状态，不能静默显示默认高校、图表或指标；由任务 2 的页面状态检查覆盖。
- 未登录用户和普通用户访问管理员接口时，必须分别得到 401/403；由任务 4 的 MockMvc 测试覆盖。
- 推荐分数为空、负数、超过合理范围时，必须在 Controller 层返回 400，不能进入推荐算法；由任务 3 和任务 4 覆盖。
- 数据库/JWT 环境变量缺失或使用本地默认值时，启动和文档行为必须明确，不得把默认凭据描述为生产安全配置；由任务 3 和任务 5 覆盖。

## File Map

- Create: `backend/src/main/java/com/univ/bigdata/vo/PredictionMetricsVo.java` — 预测模型指标与来源响应模型。
- Modify: `backend/src/main/java/com/univ/bigdata/entity/PredictionResult.java` — 保存模型结果生成时间。
- Modify: `backend/src/main/java/com/univ/bigdata/controller/PredictController.java` — 将模型指标接口返回类型切换为 `PredictionMetricsVo`。
- Modify: `backend/src/main/java/com/univ/bigdata/service/impl/PredictionResultServiceImpl.java` — 统一预测数据组装，移除静默预测默认值。
- Modify: `backend/src/main/resources/application.yml` — 增加本地配置的环境变量覆盖。
- Modify: `backend/src/main/java/com/univ/bigdata/dto/RecommendQueryDto.java` — 增加输入约束。
- Modify: `backend/src/main/java/com/univ/bigdata/dto/UniversityQueryDto.java` — 增加分页和筛选约束。
- Modify: `backend/src/main/java/com/univ/bigdata/controller/RecommendController.java` — 触发 DTO 校验。
- Modify: `backend/src/main/java/com/univ/bigdata/controller/AdminController.java` — 校验管理员分页和删除参数。
- Modify: `backend/src/main/java/com/univ/bigdata/common/exception/GlobalExceptionHandler.java` — 统一返回参数校验错误。
- Create/Modify: `backend/src/test/java/com/univ/bigdata/controller/SecurityControllerTest.java` — 认证和角色权限回归测试。
- Create/Modify: `backend/src/test/java/com/univ/bigdata/service/PredictionResultServiceTest.java` — 预测缺失数据回归测试。
- Modify: `crawler/train_predict_model.py` — 为预测结果写入统一生成时间，并输出可核对的训练指标。
- Modify: `sql/01_schema.sql` — 增加预测结果生成时间字段。
- Modify: `sql/02_init_data.sql` — 为初始化预测记录提供生成时间。
- Create: `frontend/src/utils/loadState.ts` — 前端页面统一加载状态类型和辅助函数。
- Modify: `frontend/src/api/predict.ts` — 扩展预测来源和更新时间类型。
- Modify: `frontend/src/views/predict/index.vue` — 移除静默预测数据兜底，显示真实/错误/空状态。
- Modify: `frontend/src/views/dashboard/index.vue` — 移除接口失败时的无标识默认图表数据。
- Modify: `frontend/src/views/university/index.vue` — 移除高校列表失败时的默认高校列表。
- Modify: `frontend/src/views/major/index.vue`, `frontend/src/views/clean/index.vue`, `frontend/src/views/collect/index.vue`, `frontend/src/views/enrollment/index.vue` — 统一页面错误和空数据表现。
- Modify: `frontend/src/views/admin/index.vue` — 不再把未实现的批量导入显示为已开启，并避免导出默认模拟列表。
- Modify: `README.md`, `需求分析与技术方案.md` — 更新已实现/演示模式/后续扩展矩阵。

### Task 1: 统一预测结果来源和缺失数据行为

**Files:**
- Create: `backend/src/main/java/com/univ/bigdata/vo/PredictionMetricsVo.java`
- Modify: `backend/src/main/java/com/univ/bigdata/entity/PredictionResult.java`
- Modify: `backend/src/main/java/com/univ/bigdata/service/impl/PredictionResultServiceImpl.java`
- Modify: `backend/src/main/java/com/univ/bigdata/service/PredictionResultService.java`
- Modify: `backend/src/main/resources/application.yml` only if a prediction metadata setting is needed
- Modify: `sql/01_schema.sql`
- Modify: `sql/02_init_data.sql`
- Modify: `crawler/train_predict_model.py`
- Test: `backend/src/test/java/com/univ/bigdata/service/PredictionResultServiceTest.java`

**Interfaces:**
- `PredictionMetricsVo` exposes `modelName`, `trainWindow`, `predictPeriod`, `mae`, `rmse`, `r2`, `dataSource`, and `generatedAt`.
- `PredictionResult.generatedAt` maps to `prediction_result.generated_at` using `LocalDateTime`.
- `PredictionResultService.getModelMetrics()` returns `PredictionMetricsVo`; `getTrendsData()` keeps the existing JSON shape so the existing chart API remains compatible.

- [ ] **Step 1: Write the failing service tests for missing prediction records.**

  Add tests that mock the mapper result list and assert that a missing type/year produces `null` in the corresponding trend slot, while `getModelMetrics()` returns a non-empty source and latest generation time. The test must also assert that no fallback number such as `3165.0`, `1130.0`, or `40.0` is introduced.

  ```java
  @Test
  void missingPredictionTypeShouldRemainNull() {
      when(predictionResultMapper.selectList(any())).thenReturn(List.of(
              PredictionResult.builder()
                      .type("UNIV_COUNT")
                      .year(2028)
                      .predictValue(new BigDecimal("3165.0"))
                      .generatedAt(LocalDateTime.of(2026, 9, 24, 10, 0))
                      .build()
      ));

      Map<String, Object> trends = service.getTrendsData();
      Map<String, Object> enroll = castMap(trends.get("enroll"));

      assertTrue(((List<?>) enroll.get("enrollTotal")).stream().anyMatch(Objects::isNull));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> castMap(Object value) {
      return (Map<String, Object>) value;
  }
  ```

- [ ] **Step 2: Run the focused test and confirm it fails for the current hard-coded implementation.**

  Run: `mvn.cmd -Dtest=PredictionResultServiceTest test`

  Expected: FAIL because the current service returns hard-coded fallback values and `PredictionResult` has no `generatedAt` field.

- [ ] **Step 3: Add the metadata response model and database generation timestamp.**

  Add `generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP` to `prediction_result` in the schema, add the matching field to the entity, and update initialization data to use the same timestamp for one training batch. Build `PredictionMetricsVo` with the existing model name/window/metrics plus `dataSource="prediction_result"` and the latest non-null generation time.

- [ ] **Step 4: Make the Python training script write one batch timestamp.**

  Create `generated_at = datetime.now()` once per run and change the insert statement from three values to four values:

  ```python
  generated_at = datetime.now().replace(microsecond=0)
  sql = """
      INSERT INTO prediction_result (type, year, predict_value, generated_at)
      VALUES (%s, %s, %s, %s)
  """
  cursor.executemany(
      sql,
      [(row_type, year, value, generated_at) for row_type, year, value in records_to_insert]
  )
  ```

  Keep the existing model calculations and log MAE/RMSE/R² without printing credentials.

- [ ] **Step 5: Remove prediction fallback numbers from the Java service.**

  Add a small helper such as `private Integer predictedInt(Map<Integer, Double> values, int year)` that returns `null` when absent. Replace `getOrDefault(...).intValue()` calls in `getTrendsData()` with this helper and use nullable list implementations (`Arrays.asList`) instead of `List.of` where null values are possible. Preserve the current response keys used by the Vue API.

- [ ] **Step 6: Run focused tests and compile the backend.**

  Run: `mvn.cmd -Dtest=PredictionResultServiceTest test`

  Expected: PASS, with missing values represented as `null` and metadata containing the latest generation timestamp.

### Task 2: Establish explicit frontend live/empty/error states

**Files:**
- Create: `frontend/src/utils/loadState.ts`
- Modify: `frontend/src/api/predict.ts`
- Modify: `frontend/src/views/predict/index.vue`
- Modify: `frontend/src/views/dashboard/index.vue`
- Modify: `frontend/src/views/university/index.vue`
- Modify: `frontend/src/views/major/index.vue`
- Modify: `frontend/src/views/clean/index.vue`
- Modify: `frontend/src/views/collect/index.vue`
- Modify: `frontend/src/views/enrollment/index.vue`
- Modify: `frontend/src/views/admin/index.vue`

**Interfaces:**
- `LoadState = 'idle' | 'loading' | 'success' | 'empty' | 'error'`.
- `createLoadState()` returns `{ state: Ref<LoadState>, errorMessage: Ref<string>, start(): void, succeed(isEmpty?: boolean): void, fail(message: string): void }`.
- `ModelMetricsData` adds optional `dataSource?: string` and `generatedAt?: string` without breaking existing API responses.

- [ ] **Step 1: Add the shared load-state helper.**

  Create `loadState.ts` with this implementation shape:

  ```ts
  import { ref, type Ref } from 'vue'

  export type LoadState = 'idle' | 'loading' | 'success' | 'empty' | 'error'

  export interface LoadStateController {
    state: Ref<LoadState>
    errorMessage: Ref<string>
    start: () => void
    succeed: (empty?: boolean) => void
    fail: (message: string) => void
  }

  export function createLoadState(): LoadStateController {
    const state = ref<LoadState>('idle')
    const errorMessage = ref('')
    return {
      state,
      errorMessage,
      start: () => { state.value = 'loading'; errorMessage.value = '' },
      succeed: (empty = false) => { state.value = empty ? 'empty' : 'success' },
      fail: (message: string) => { state.value = 'error'; errorMessage.value = message }
    }
  }
  ```

  Import `ref` from Vue and keep the helper presentation-agnostic.

- [ ] **Step 2: Refactor the prediction page first.**

  Remove the hard-coded `metrics`, history, major, enrollment, and population fallback arrays. During `loadData()`, call `start()`, load both API resources, treat missing/empty trend sections as `empty`, and on failure call `fail()` without drawing a fake chart. Add an inline status element with retry behavior and show `dataSource`/`generatedAt` when present.

- [ ] **Step 3: Refactor dashboard, university, major, clean, collect, and enrollment fallbacks.**

  For each existing `catch {}` that assigns default chart/list data, replace it with the shared state and an explicit empty/error view. Keep the existing successful chart rendering and API response mapping unchanged. A request failure must leave the chart container empty or show a retry action; it must not render a hard-coded national statistic.

- [ ] **Step 4: Correct admin-page messaging and export behavior.**

  Change `handleBatchImport()` to clearly state that batch import is a planned extension. Change `handleExportExcel()` so it exports only successfully loaded `tableData`; when the current query has no loaded rows, show a warning and do not export `defaultMockList`. Keep the current CSV export as a documented lightweight export, not as EasyExcel.

- [ ] **Step 5: Build the frontend and manually inspect the two most affected states.**

  Run: `npm.cmd run build`

  Expected: PASS. With the API unavailable, the prediction and university pages show an explicit error/empty state; with the API available, existing charts and tables still render.

### Task 3: Tighten request validation and environment-backed configuration

**Files:**
- Modify: `backend/src/main/resources/application.yml`
- Modify: `backend/src/main/java/com/univ/bigdata/dto/RecommendQueryDto.java`
- Modify: `backend/src/main/java/com/univ/bigdata/dto/UniversityQueryDto.java`
- Modify: `backend/src/main/java/com/univ/bigdata/controller/RecommendController.java`
- Modify: `backend/src/main/java/com/univ/bigdata/controller/AdminController.java`
- Modify: `backend/src/main/java/com/univ/bigdata/common/exception/GlobalExceptionHandler.java`

**Interfaces:**
- Invalid recommendation requests return the existing `Result` error format with HTTP 400.
- Page size is constrained to `1..100`; page number is at least `1`.
- The local configuration keeps compatible defaults while supporting `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `JWT_SECRET` overrides.

- [ ] **Step 1: Add failing validation tests for invalid recommendation and pagination input.**

  Add MockMvc cases for an empty recommendation body, negative score, score above 900, `page=0`, and `size=101`. Assert HTTP 400 and a non-empty error message.

  ```java
  mockMvc.perform(post("/api/recommend/match")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"province\":\"湖南\",\"score\":-1}"))
      .andExpect(status().isBadRequest());
  ```

- [ ] **Step 2: Run the validation tests and confirm current DTOs accept invalid values.**

  Run: `mvn.cmd -Dtest=ValidationControllerTest test`

  Expected: FAIL before annotations and controller validation are added.

- [ ] **Step 3: Add Bean Validation constraints and activate them.**

  Use `@NotBlank` for required recommendation text fields, `@NotNull`, `@DecimalMin("0.0")`, and `@DecimalMax("900.0")` for `score`, `@Min(1)` for page, and `@Min(1) @Max(100)` for size. Add `@Valid` to the request body and query model parameters. Use `@Positive` for the administrator delete ID.

- [ ] **Step 4: Normalize validation errors in the global handler.**

  Handle `MethodArgumentNotValidException`, `BindException`, and `ConstraintViolationException` by returning the first useful field message through `Result.failed(...)`, without returning stack traces or database details.

- [ ] **Step 5: Move local configuration values behind environment variables.**

  Update the default YAML to use the same variable names already used in `application-prod.yml`, while retaining local-only fallback values for existing startup compatibility:

  ```yaml
  spring:
    datasource:
      url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/univ_bigdata_db?...}
      username: ${SPRING_DATASOURCE_USERNAME:root}
      password: ${SPRING_DATASOURCE_PASSWORD:123456}
  jwt:
    secret: ${JWT_SECRET:local-development-secret-change-me}
  ```

  Update README so the fallback is explicitly described as local development only.

- [ ] **Step 6: Run validation tests and backend tests.**

  Run: `mvn.cmd -Dtest=ValidationControllerTest,SecurityControllerTest,RecommendServiceTest test`

  Expected: PASS, with invalid inputs rejected before service execution.

### Task 4: Add authentication, authorization, and prediction regression coverage

**Files:**
- Create: `backend/src/test/java/com/univ/bigdata/controller/SecurityControllerTest.java`
- Create or modify: `backend/src/test/java/com/univ/bigdata/controller/ValidationControllerTest.java`
- Create: `backend/src/test/java/com/univ/bigdata/service/PredictionResultServiceTest.java`
- Modify: `backend/src/test/java/com/univ/bigdata/service/RecommendServiceTest.java` only if setup is reusable

**Interfaces:**
- Tests use the existing Spring Boot application and local database configuration; no production writes are allowed.
- Security assertions use the seeded `admin` and `user` roles only when the local test database is available; otherwise the test report must mark the integration check unverified rather than replacing it with a fake success.

- [ ] **Step 1: Add security tests for public, unauthenticated, and role-protected endpoints.**

  Cover:

  ```java
  mockMvc.perform(get("/api/university/page"))
      .andExpect(status().isOk());

  mockMvc.perform(get("/api/admin/university/page"))
      .andExpect(status().isUnauthorized());

  // obtain a token through /api/auth/login with the seeded ordinary user,
  // then send it to the administrator endpoint and expect 403.
  ```

- [ ] **Step 2: Add the administrator success-path test.**

  Log in with the seeded admin account, call `/api/admin/university/page`, and assert HTTP 200 plus a `records`/`total` page response. Do not add or delete rows in this test.

- [ ] **Step 3: Add prediction response tests.**

  Assert that model metadata contains source and generation time, trend arrays preserve `null` for unavailable predictions, and the response still contains `univ`, `major`, and `enroll` keys.

- [ ] **Step 4: Run the complete backend test suite.**

  Run: `mvn.cmd test`

  Expected: PASS for all available tests. Record database-dependent limitations separately if the local database is unavailable.

### Task 5: Align documentation and produce the答辩 evidence checklist

**Files:**
- Modify: `README.md`
- Modify: `需求分析与技术方案.md`
- Modify: `crawler/README.md` only where the actual training/validation path is described

**Interfaces:**
- Documentation uses the labels `已实现`, `演示模式`, and `后续扩展` consistently.
- No documentation claims EasyExcel, Redis, PDF export, password editing, or batch import as completed unless the implementation and verification exist.

- [ ] **Step 1: Add an implementation matrix to README.**

  List the verified core modules: dashboard, university query/detail, comparison, recommendation, prediction display, data collection/cleaning display, JWT/RBAC, and administrator university CRUD. Add the unimplemented items under `后续扩展`.

- [ ] **Step 2: Correct technical-stack and performance claims.**

  Remove or relabel Redis, EasyExcel, PDF, “百万级流式导出”, “QPS 提升 6 倍”, and “40ms/50MB” claims unless there is a checked-in implementation and measurement record. Keep the actual Spring Boot/Vue/MySQL/Python/ECharts stack.

- [ ] **Step 3: Add a reproducible verification section.**

  Document the three commands and what each proves:

  ```text
  frontend: npm.cmd run build
  backend:  mvn.cmd test
  crawler:  py -3 -m compileall -q crawler
  ```

  Explain that build/compile success is not the same as live database or remote crawler validation.

- [ ] **Step 4: Run a final claim audit.**

  Search the documentation for `EasyExcel`, `Redis`, `PDF`, `百万级`, `QPS`, and `50MB`; every remaining occurrence must be labeled as architecture option, planned extension, or verified evidence with a corresponding file/test.

- [ ] **Step 5: Run final verification and inspect the diff.**

  Run:

  ```powershell
  npm.cmd run build
  mvn.cmd test
  py -3 -m compileall -q crawler
  git diff --check
  git status --short
  ```

  Expected: all available checks pass, whitespace errors are absent, and the diff contains only files listed in this plan plus the already approved design/plan documents.
