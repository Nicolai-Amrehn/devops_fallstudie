import { test, expect } from '@playwright/test';

test.describe('E2E-02 Anmeldung', () => {
  test('falsches Passwort bleibt beim Identity Provider mit Fehlermeldung', async ({ page }) => {
    await page.goto('/');
    await page.getByTestId('nav-login').click();
    await page.getByLabel('Username or email').fill('anna');
    await page.getByLabel('Password', { exact: true }).fill('falsch');
    await page.getByRole('button', { name: 'Sign In' }).click();

    await expect(page.getByText('Invalid username or password.')).toBeVisible();
    await expect(page).not.toHaveURL(/\/checkout/);
  });

  test('Kasse ohne Anmeldung führt zur Anmeldemaske', async ({ page }) => {
    await page.goto('/checkout');
    await expect(page.getByRole('button', { name: 'Sign In' })).toBeVisible();
  });
});
