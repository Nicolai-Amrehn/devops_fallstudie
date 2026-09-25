import { Page, expect } from '@playwright/test';

export const ANNA = { username: 'anna', password: 'anna123' };

/** Meldet sich über die Login-Maske des Identity Providers an. */
export async function login(page: Page, user = ANNA) {
  await page.getByTestId('nav-login').click();
  await page.getByLabel('Username or email').fill(user.username);
  await page.getByLabel('Password', { exact: true }).fill(user.password);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await expect(page.getByTestId('nav-user')).toHaveText(user.username);
}

/** Legt eine Variante über die Artikelseite in den Warenkorb. */
export async function addToCart(page: Page, productId: number, sku: string, quantity = 1) {
  await page.goto(`/products/${productId}`);
  const row = page.getByTestId(`variant-${sku}`);
  await row.getByLabel('Menge').fill(String(quantity));
  await row.getByTestId('add-to-cart').click();
  await expect(page.getByTestId(`cart-line-${sku}`)).toBeVisible();
}
