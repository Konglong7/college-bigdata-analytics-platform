<template>
  <div class="login-wrapper">
    <div class="dv-border-box login-box">

      <div class="login-header">
        <div class="header-badge">BIGDATA PLATFORM</div>
        <h2>全国高校大数据分析可视化平台</h2>
        <p>{{ isRegister ? '新用户账号注册' : '欢迎登录系统' }}</p>
      </div>

      <el-form :model="form" class="login-form">
        <el-form-item>
          <div class="form-input-wrap">
            <span class="input-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </span>
            <input
              v-model="form.username"
              type="text"
              class="dv-input"
              placeholder="请输入用户名 (如: admin)"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-input-wrap">
            <span class="input-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </span>
            <input
              v-model="form.password"
              type="password"
              class="dv-input"
              placeholder="请输入密码 (如: admin123)"
              @keyup.enter="handleSubmit"
            />
          </div>
        </el-form-item>

        <!-- 验证码校验项 -->
        <el-form-item>
          <div class="captcha-row">
            <div class="form-input-wrap captcha-input-wrap">
              <span class="input-icon">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
              </span>
              <input
                v-model="form.captcha"
                type="text"
                class="dv-input"
                maxlength="6"
                placeholder="请输入验证码"
                @keyup.enter="handleSubmit"
              />
            </div>
            <Captcha ref="captchaRef" :width="120" :height="42" />
          </div>
        </el-form-item>

        <div class="form-actions">
          <button class="dv-btn submit-btn" :disabled="loading" @click.prevent="handleSubmit">
            {{ loading ? '处理中...' : (isRegister ? '立即注册' : '登 录 系 统') }}
          </button>
        </div>

        <div class="form-footer">
          <span class="toggle-link" @click="toggleMode">
            {{ isRegister ? '已有账号？去登录' : '没有账号？立即注册' }}
          </span>
          <span v-if="!isRegister" class="quick-tip" @click="fillAdmin">一键填入管理员</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'
import Captcha from '@/components/Captcha/index.vue'

const router = useRouter()
const isRegister = ref(false)
const loading = ref(false)
const captchaRef = ref<InstanceType<typeof Captcha> | null>(null)

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const fillAdmin = () => {
  form.username = 'admin'
  form.password = 'admin123'
  // 若验证码组件已生成，可自动对齐或引导输入
  if (captchaRef.value) {
    form.captcha = captchaRef.value.getCode()
  }
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  form.captcha = ''
  captchaRef.value?.refresh()
}

const handleSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  if (!form.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }

  if (!captchaRef.value?.verify(form.captcha)) {
    ElMessage.error('验证码输入有误，请重新输入')
    form.captcha = ''
    captchaRef.value?.refresh()
    return
  }

  loading.value = true
  try {
    if (isRegister.value) {
      await register({
        username: form.username,
        password: form.password
      })
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
      form.captcha = ''
      captchaRef.value?.refresh()
    } else {
      const res = await login({
        username: form.username,
        password: form.password
      })
      if (res && res.token) {
        localStorage.setItem('token', res.token)
        localStorage.setItem('username', res.username)
        localStorage.setItem('role', res.role)
        ElMessage.success('登录成功，正在进入平台...')
        router.push('/dashboard')
      }
    }
  } catch (e: any) {
    // 刷新验证码
    form.captcha = ''
    captchaRef.value?.refresh()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-wrapper {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-main);
  background-image: 
      radial-gradient(ellipse at center, rgba(16, 137, 255, 0.15) 0%, rgba(2, 9, 27, 0.95) 75%),
      linear-gradient(rgba(0, 229, 255, 0.04) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 229, 255, 0.04) 1px, transparent 1px);
  background-size: 100% 100%, 40px 40px, 40px 40px;
}

.login-box {
  width: 440px;
  padding: 40px 36px;
  background: rgba(13, 22, 41, 0.9);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6), 0 0 30px rgba(56, 189, 248, 0.15);
  border: 1px solid var(--border-color);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--card-radius);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;

  .header-badge {
    display: inline-block;
    padding: 3px 10px;
    font-size: 10.5px;
    letter-spacing: 2px;
    color: var(--primary-light);
    background: rgba(56, 189, 248, 0.1);
    border: 1px solid rgba(56, 189, 248, 0.25);
    border-radius: 20px;
    margin-bottom: 12px;
    font-family: var(--font-mono);
  }

  h2 {
    font-size: 20px;
    letter-spacing: 1.5px;
    font-weight: 700;
    color: #ffffff;
    margin-bottom: 8px;
    background: linear-gradient(180deg, #ffffff 0%, #e0f2fe 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  p {
    font-size: 13px;
    color: var(--text-sub);
  }
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-input-wrap {
  width: 100%;
  display: flex;
  align-items: center;
  position: relative;

  .input-icon {
    position: absolute;
    left: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-muted);
    pointer-events: none;
    transition: color 0.2s;
  }

  .dv-input, .dv-select {
    width: 100%;
    padding-left: 42px;
    height: 44px;
    background: rgba(10, 18, 36, 0.65);
    border: 1px solid var(--border-color);
    color: var(--text-main);
    border-radius: 6px;
    outline: none;
    font-size: 13.5px;
    transition: all 0.2s ease;

    &::placeholder {
      color: var(--text-muted);
    }

    &:focus {
      border-color: var(--primary-light);
      box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.2);
      background: rgba(10, 18, 36, 0.85);

      & ~ .input-icon {
        color: var(--primary-light);
      }
    }
  }

  .dv-select {
    cursor: pointer;
    option {
      background: #0d1629;
      color: #fff;
    }
  }
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
  align-items: center;

  .captcha-input-wrap {
    flex: 1;
  }
}

.form-actions {
  margin-top: 10px;

  .submit-btn {
    width: 100%;
    height: 44px;
    font-size: 14.5px;
    font-weight: 600;
    letter-spacing: 2px;
    background: linear-gradient(135deg, #0ea5e9 0%, #3b82f6 100%);
    border: 1px solid rgba(56, 189, 248, 0.4);
    color: #ffffff;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 4px 14px rgba(14, 165, 233, 0.3);

    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #38bdf8 0%, #2563eb 100%);
      box-shadow: 0 6px 20px rgba(14, 165, 233, 0.45);
      transform: translateY(-1px);
    }

    &:active:not(:disabled) {
      transform: translateY(0);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

.form-footer {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-sub);
  margin-top: 14px;

  .toggle-link, .quick-tip {
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: var(--primary-light);
    }
  }

  .quick-tip {
    color: var(--warning);
    &:hover {
      color: #fcd34d;
    }
  }
}
</style>
