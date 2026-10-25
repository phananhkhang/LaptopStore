$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Path

$images = @(
    @{ Name = 'Dell Inspiron 14 5430'; File = 'product-01.jpg'; Url = 'https://technave.com/data/files/article/202304040323288959.jpg' },
    @{ Name = 'Dell Latitude 5440'; File = 'product-02.jpg'; Url = 'https://phucanhcdn.com/media/product/52799_laptop_dell_latitude_5440_42lt544001_1.jpg' },
    @{ Name = 'Dell G15 5530'; File = 'product-03.png'; Url = 'https://laptopkhanhtran.vn/pic/product/Dell_Gami_638219236669530959_HasThumb.png' },
    @{ Name = 'Dell XPS 15 9530'; File = 'product-04.jpg'; Url = 'https://bizweb.dktcdn.net/100/512/769/products/dell-xps-9530.jpg?v=1719650060813' },
    @{ Name = 'Dell Vostro 14 3430'; File = 'product-05.png'; Url = 'https://www.dellonline.co.za/cdn/shop/products/shopify-favicon_4d767d1f-443e-4013-8b72-c33fe19efd4a.png?v=1717599152' },
    @{ Name = 'HP Pavilion 14'; File = 'product-06.jpg'; Url = 'https://static.fnac-static.com/multimedia/Images/FR/MDM/6b/40/2e/19808363/3756-1/tsp20260218212626/PC-Ultra-Portable-HP-Pavilion-14-dv2123nf-14-Intel-Core-i5-16-Go-RAM-512-Go-D-Argent-naturel.jpg' },
    @{ Name = 'HP ProBook 440 G10'; File = 'product-07.jpg'; Url = 'https://brightstarcomp.com/cdn/shop/files/Web_ProBook_440_G10_1.jpg?v=1752120148' },
    @{ Name = 'HP Victus 16'; File = 'product-08.jpg'; Url = 'https://cdn.idealo.com/folder/Product/203054/2/203054201/s1_produktbild_max/hp-victus-16-s0777ng.jpg' },
    @{ Name = 'HP Envy 16'; File = 'product-09.jpg'; Url = 'https://www.notebookcheck-cn.com/uploads/tx_nbc2/envy16.jpg' },
    @{ Name = 'HP Laptop 15s'; File = 'product-10.jpg'; Url = 'https://cdn.idealo.com/folder/Product/202016/4/202016457/s3_produktbild_max/hp-15s-2022.jpg' },
    @{ Name = 'Lenovo ThinkPad E14 Gen 5'; File = 'product-11.jpg'; Url = 'https://phucanhcdn.com/media/lib/12-02-2025/thinkpad_e14_gen_5_intel_ct1_031_2_11zon.jpg' },
    @{ Name = 'Lenovo IdeaPad Slim 5 14'; File = 'product-12.jpg'; Url = 'https://media.s-bol.com/DoLrBgom4x4K/ADMXEYO/550x458.jpg' },
    @{ Name = 'Lenovo LOQ 15'; File = 'product-13.jpg'; Url = 'https://cdn.idealo.com/folder/Product/205807/9/205807932/s1_produktbild_max/lenovo-loq-15-83gs00k4ge.jpg' },
    @{ Name = 'Lenovo Legion 5 16'; File = 'product-14.webp'; Url = 'https://sota.store/image/cache/catalog/Lenovo/Lenovo-83DG002NGE-1600x1600.webp' },
    @{ Name = 'Lenovo Yoga Pro 7'; File = 'product-15.jpg'; Url = 'https://www.virginmegastore.ae/medias/1002842-main.jpg?context=bWFzdGVyfHJvb3R8NzY2MzZ8aW1hZ2UvanBlZ3xhR0ZtTDJnM1pTOHhNRFUwTURBek1qQTJOVFUyTmk4eE1EQXlPRFF5WDE5dFlXbHVMbXB3Wnd8OTZjZDI0ZTY4N2RlZDg3YjIzZDQzYTBkOWIwMjQ3ZDA3NmFjMzcxNzE3ZmYxN2EzZGY4YzY2MzlhNmJlZjkxMQ' },
    @{ Name = 'ASUS Vivobook 15 OLED'; File = 'product-16.jpg'; Url = 'https://media.currys.biz/i/currysprod/10265767' },
    @{ Name = 'ASUS Zenbook 14 OLED'; File = 'product-17.jpg'; Url = 'https://id.store.asus.com/media/catalog/product/z/e/zenbook_14_oled_ux3405ma_product_photo_3s_foggy_silver_03_1.jpg' },
    @{ Name = 'ASUS TUF Gaming A15'; File = 'product-18.jpg'; Url = 'https://dlcdnwebimgs.asus.com/gain/ed4c3cdc-29c4-4bdf-a42b-9f008244a173/' },
    @{ Name = 'ASUS ROG Strix G16'; File = 'product-19.jpg'; Url = 'https://in.store.asus.com/media/catalog/product/g/6/g614jv_eclps_gry_4zn_rgb_kbd_mso_rfrsh_1_.jpg' },
    @{ Name = 'ASUS ProArt Studiobook 16'; File = 'product-20.jpeg'; Url = 'https://i5.walmartimages.com/seo/ASUS-ProArt-StudioBook-16-Home-Business-Laptop-Intel-i7-11800H-8-Core-32GB-RAM-1TB-SSD-16-3840x2400-NVIDIA-RTX-3060-Wifi-Bluetooth-Webcam-1xHDMI-Back_116017af-b880-49bf-afd6-51704a63f250.758ee238f5239171fdc1b1382490232c.jpeg' },
    @{ Name = 'Acer Aspire 5 15'; File = 'product-21.png'; Url = 'https://www.firstshop.co.za/cdn/shop/files/nx-khfea-001-traditional-laptops-42899699957924.png?v=1685692014&width=1214' },
    @{ Name = 'Acer Swift Go 14 OLED'; File = 'product-22.jpg'; Url = 'https://ecommerce.datablitz.com.ph/cdn/shop/files/asg56asd4gds_1024x.jpg?v=1777257336' },
    @{ Name = 'Acer Nitro V 15'; File = 'product-23.png'; Url = 'https://www.payngo.co.il/cdn-cgi/image/format%3Dauto%2Cmetadata%3Dnone%2Cquality%3D90%2Cwidth%3D700%2Cheight%3D700/media/catalog/product/0/-/0-etailer-hero-acer-nitro-v-15-anv15-52-non-fingerprint-with-backlit-on-wp-logo-black-01-custom_1__2.png' },
    @{ Name = 'Acer Predator Helios Neo 16'; File = 'product-24.png'; Url = 'https://product.hstatic.net/1000233206/product/acer-predator-helios-neo-16-phn16-72-78dq_39d9e758663548c88edd95c6fcf8c1eb_1024x1024.png' },
    @{ Name = 'Acer TravelMate P2'; File = 'product-25.jpg'; Url = 'https://partspc.pl/1629786-large_default/notebook-acer-travelmate-p2-tmp214-55-g2-tco.jpg' },
    @{ Name = 'MacBook Air 13 M2'; File = 'product-26.jpg'; Url = 'https://image.coupangcdn.com/image/vendor_inventory/c54f/b14a4fc55db7ac84929955cb9de2785abaf6e2c37d21029f06df79a5af6d.jpg' },
    @{ Name = 'MacBook Air 15 M3'; File = 'product-27.jpg'; Url = 'https://www.costco.com.au/medias/sys_master/images/hcf/h04/221767961018398.jpg' },
    @{ Name = 'MacBook Pro 14 M3 Pro'; File = 'product-28.jpg'; Url = 'https://media.falabella.com/falabellaCL/16997775_1/w%3D800%2Ch%3D800%2Cfit%3Dpad' },
    @{ Name = 'MacBook Pro 16 M3 Pro'; File = 'product-29.png'; Url = 'https://hoanghamobile.com/Uploads/2023/11/17/macbook-pro-16-m3-pro-max-space-black-front.png' },
    @{ Name = 'MacBook Air 13 M3'; File = 'product-30.jpg'; Url = 'https://cdn.lesnumeriques.com/optim/product/74/74179/af99a8ef-macbook-air-m3_png__1200_900__overflow.jpg' }
)

$headers = @{
    'User-Agent' = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/155 Safari/537.36'
    'Accept' = 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8'
}

$ok = 0
$failed = New-Object System.Collections.Generic.List[string]

foreach ($img in $images) {
    $out = Join-Path $here $img.File
    Write-Host ("Downloading {0} -> {1}" -f $img.Name, $img.File)

    try {
        Invoke-WebRequest -Uri $img.Url -OutFile $out -UseBasicParsing -Headers $headers -MaximumRedirection 10 -TimeoutSec 45

        if (-not (Test-Path $out) -or (Get-Item $out).Length -lt 1024) {
            throw "Downloaded file is missing or too small"
        }

        $ok++
        Write-Host '  OK'
    }
    catch {
        if (Test-Path $out) { Remove-Item $out -Force -ErrorAction SilentlyContinue }
        $failed.Add($img.Name)
        Write-Warning ("FAILED: {0} - {1}" -f $img.Name, $_.Exception.Message)
    }
}

Write-Host ''
Write-Host ("Finished: {0}/{1} images downloaded." -f $ok, $images.Count)

if ($failed.Count -gt 0) {
    Write-Host 'Failed products:'
    foreach ($name in $failed) {
        Write-Host (" - {0}" -f $name)
    }
    Write-Host ''
    Write-Host 'Some sites may block hotlink downloads. Re-run later or replace only the failed image URLs.'
    exit 1
}

Write-Host 'All 30 images downloaded successfully into uploads/products.'
