import { defineConfig, devices } from '@playwright/test';

const isCI = !!process.env['CI'];

export const WEB_URL = process.env['WEB_URL'] ?? 'http://localhost:3000';
export const USER_API_URL =
  process.env['USER_API_URL'] ?? 'http://localhost:8081';

export default defineConfig({
  testDir: './src/e2e',
  outputDir: './test-results',
  fullyParallel: true,
  forbidOnly: isCI,
  retries: isCI ? 2 : 0,
  workers: isCI ? 1 : undefined,
  reporter: isCI
    ? [
        ['html', { outputFolder: './playwright-report', open: 'never' }],
        ['github'],
      ]
    : [['html', { outputFolder: './playwright-report', open: 'on-failure' }]],

  use: {
    baseURL: WEB_URL,
    trace: isCI ? 'on-first-retry' : 'retain-on-failure',
    screenshot: 'only-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  ...(isCI
    ? {}
    : {
        webServer: {
          command: 'pnpm nx dev @mono-repo/web',
          url: WEB_URL,
          reuseExistingServer: true,
          timeout: 60_000,
          cwd: '../..',
        },
      }),
});
