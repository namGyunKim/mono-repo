import { test, expect } from '@playwright/test';

test.describe('Home Page', () => {
  test('should render welcome message', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('#welcome h1')).toContainText(
      'Welcome @mono-repo/web'
    );
  });
});
