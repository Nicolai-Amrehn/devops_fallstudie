import { defineConfig, devices } from '@playwright/test';

/**
 * Das SUT ist der Compose-Stack. Lokal: BASE_URL=http://localhost:8080 (mit Hosts-Eintrag
 * „127.0.0.1 keycloak"), im Compose-Profil e2e: http://webshop:8080.
 */
export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  expect: { timeout: 5_000 },
  fullyParallel: false,
  workers: 1,
  // Wiederholungen nur in CI, lokal fällt ein flackernder Test sofort auf.
  retries: process.env.CI ? 2 : 0,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['junit', { outputFile: 'test-results/junit-e2e.xml' }],
    ['allure-playwright', { resultsDir: 'allure-results' }],
  ],
  use: {
    baseURL: process.env.BASE_URL ?? 'http://localhost:8080',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    locale: 'de-DE',
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        // Chromium versucht bei Klartext-URLs zuerst HTTPS („HTTPS-Upgrades"). Gegen einen
        // reinen HTTP-Container im Compose-Netz führt das zu ERR_SSL_PROTOCOL_ERROR.
        launchOptions: {
          args: ['--disable-features=HttpsUpgrades,HttpsFirstBalancedModeAutoEnable'],
        },
      },
    },
  ],
});
