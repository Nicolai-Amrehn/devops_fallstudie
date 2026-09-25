import { test, expect } from '@playwright/test';

test.describe('E2E-03 Verfügbarkeit je Variante', () => {
  test('ausverkaufte Variante ist nicht bestellbar, Schwestervariante schon', async ({ page }) => {
    await page.goto('/products/1');
    await expect(page.getByTestId('product-name')).toHaveText('Stratos Classic E-Gitarre');

    const soldOut = page.getByTestId('variant-GIT-STRAT-SB-L');
    await expect(soldOut.getByTestId('variant-availability')).toHaveText('ausverkauft');
    await expect(soldOut.getByTestId('sold-out')).toBeDisabled();

    const available = page.getByTestId('variant-GIT-STRAT-SB-R');
    await expect(available.getByTestId('variant-availability')).toHaveText('verfügbar');
    await expect(available.getByTestId('add-to-cart')).toBeEnabled();
  });

  test('Suche ohne Treffer meldet sich sauber', async ({ page }) => {
    await page.goto('/?q=Cembalo');
    await expect(page.getByTestId('no-results')).toBeVisible();
  });
});
