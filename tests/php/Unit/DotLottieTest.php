<?php

use Jubianchi\NativePHP\LottieRefreshIndicator\DotLottie;

beforeEach(function () {
    $this->dir = temporaryDirectory();
});

it('recognizes a dotLottie v2 file', function () {
    expect(DotLottie::isV2(makeDotLottieV2("{$this->dir}/logo.lottie", ['layers' => []])))->toBeTrue();
});

it('does not take a dotLottie v1 file for a v2 one', function () {
    expect(DotLottie::isV2(makeDotLottieV1("{$this->dir}/logo.lottie", ['layers' => []])))->toBeFalse();
});

it('does not take a file without manifest or a non-zip file for a v2 one', function () {
    $noManifest = makeZip("{$this->dir}/no-manifest.lottie", ['animations/main.json' => '{}']);
    file_put_contents("{$this->dir}/logo.json", '{"layers":[]}');

    expect(DotLottie::isV2($noManifest))->toBeFalse()
        ->and(DotLottie::isV2("{$this->dir}/logo.json"))->toBeFalse();
});

it('converts a v2 file into a v1 file lottie-ios reads', function () {
    $source = makeDotLottieV2("{$this->dir}/logo.lottie", ['nm' => 'logo', 'fr' => 30, 'op' => 60, 'layers' => [['nm' => 'Shape', 'ty' => 4]]]);

    expect(DotLottie::toV1($source, "{$this->dir}/out/logo.lottie"))->toBeTrue();

    $entries = zipEntries("{$this->dir}/out/logo.lottie");
    expect(array_keys($entries))->toBe(['animations/main.json', 'manifest.json'])
        ->and(json_decode($entries['manifest.json'], true))->toBe([
            'version' => '1.0',
            'animations' => [['id' => 'main', 'mode' => 'normal', 'direction' => 1]],
        ])
        ->and(json_decode($entries['animations/main.json'], true))->toBe([
            'nm' => 'main', 'fr' => 30, 'op' => 60, 'layers' => [['nm' => 'Shape', 'ty' => 4]],
        ]);
});

it('strips what the splashscreen plugin strips for lottie-ios', function () {
    $source = makeDotLottieV2("{$this->dir}/logo.lottie", [
        'fonts' => ['list' => []],
        'layers' => [
            ['nm' => 'Background', 'ty' => 1],
            ['nm' => 'Title', 'ty' => 5],
            ['nm' => 'Shape', 'ty' => 4, 'ef' => [['ty' => 5]], 'hasMask' => true],
        ],
    ]);

    DotLottie::toV1($source, "{$this->dir}/out/logo.lottie");

    expect(json_decode(zipEntries("{$this->dir}/out/logo.lottie")['animations/main.json'], true))->toBe([
        'layers' => [['nm' => 'Shape', 'ty' => 4]],
        'nm' => 'main',
    ]);
});

it('fails to convert a v2 file without animation', function () {
    $source = makeZip("{$this->dir}/logo.lottie", ['manifest.json' => '{"version":"2"}']);

    expect(DotLottie::toV1($source, "{$this->dir}/out/logo.lottie"))->toBeFalse()
        ->and("{$this->dir}/out/logo.lottie")->not->toBeFile();
});

it('compares animation files by content', function () {
    $a = makeDotLottieV1("{$this->dir}/a.lottie", ['layers' => []]);
    $b = makeDotLottieV1("{$this->dir}/b.lottie", ['layers' => []]);
    $c = makeDotLottieV1("{$this->dir}/c.lottie", ['layers' => [['ty' => 4]]]);
    file_put_contents("{$this->dir}/a.json", '{"layers":[]}');
    file_put_contents("{$this->dir}/b.json", '{"layers":[]}');

    touch($b, time() - 3600);

    expect(DotLottie::sameContent($a, $b))->toBeTrue()
        ->and(DotLottie::sameContent($a, $c))->toBeFalse()
        ->and(DotLottie::sameContent("{$this->dir}/a.json", "{$this->dir}/b.json"))->toBeTrue()
        ->and(DotLottie::sameContent("{$this->dir}/a.json", $a))->toBeFalse()
        ->and(DotLottie::sameContent($a, "{$this->dir}/missing.lottie"))->toBeFalse();
});
