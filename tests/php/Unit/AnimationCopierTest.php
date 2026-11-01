<?php

use Jubianchi\NativePHP\LottieRefreshIndicator\AnimationCopier;
use Jubianchi\NativePHP\LottieRefreshIndicator\DotLottie;

beforeEach(function () {
    $this->source = temporaryDirectory();
    $this->build = temporaryDirectory();
    $this->android = "{$this->build}/app/src/main/assets/animations";
    $this->ios = "{$this->build}/NativePHP/Resources/animations";
});

it('copies the animations to the Android assets, keeping their paths', function () {
    makeDotLottieV1("{$this->source}/splash.lottie", ['layers' => []]);
    makeDotLottieV1("{$this->source}/brand/logo.lottie", ['layers' => []]);
    file_put_contents("{$this->source}/brand/wave.json", '{"layers":[]}');

    $result = AnimationCopier::copy($this->source, $this->build, 'android');

    expect($result->copied)->toBe(['animations/brand/logo.lottie', 'animations/brand/wave.json', 'animations/splash.lottie'])
        ->and($result->warnings)->toBe([])
        ->and(file_get_contents("{$this->android}/splash.lottie"))->toBe(file_get_contents("{$this->source}/splash.lottie"))
        ->and(file_get_contents("{$this->android}/brand/logo.lottie"))->toBe(file_get_contents("{$this->source}/brand/logo.lottie"))
        ->and(file_get_contents("{$this->android}/brand/wave.json"))->toBe('{"layers":[]}');
});

it('copies the animations to the iOS resources, keeping their paths', function () {
    makeDotLottieV1("{$this->source}/brand/logo.lottie", ['layers' => []]);
    file_put_contents("{$this->source}/wave.json", '{"layers":[]}');

    $result = AnimationCopier::copy($this->source, $this->build, 'ios');

    expect($result->copied)->toBe(['animations/brand/logo.lottie', 'animations/wave.json'])
        ->and(file_get_contents("{$this->ios}/brand/logo.lottie"))->toBe(file_get_contents("{$this->source}/brand/logo.lottie"))
        ->and(file_get_contents("{$this->ios}/wave.json"))->toBe('{"layers":[]}');
});

it('leaves out files that are not animations', function () {
    file_put_contents("{$this->source}/README.md", 'notes');
    file_put_contents("{$this->source}/.DS_Store", '');
    file_put_contents("{$this->source}/wave.json", '{"layers":[]}');

    expect(AnimationCopier::copy($this->source, $this->build, 'android')->copied)->toBe(['animations/wave.json'])
        ->and("{$this->android}/README.md")->not->toBeFile();
});

it('converts dotLottie v2 animations for iOS only', function () {
    $v2 = makeDotLottieV2("{$this->source}/logo.lottie", ['layers' => [['nm' => 'Shape', 'ty' => 4]]]);
    DotLottie::toV1($v2, "{$this->build}/expected.lottie");

    AnimationCopier::copy($this->source, $this->build, 'ios');
    AnimationCopier::copy($this->source, $this->build, 'android');

    expect(DotLottie::isV2("{$this->ios}/logo.lottie"))->toBeFalse()
        ->and(DotLottie::sameContent("{$this->ios}/logo.lottie", "{$this->build}/expected.lottie"))->toBeTrue()
        ->and(file_get_contents("{$this->android}/logo.lottie"))->toBe(file_get_contents($v2));
});

it('copies a v2 animation it cannot convert as is, with a warning', function () {
    $broken = makeZip("{$this->source}/logo.lottie", ['manifest.json' => '{"version":"2"}']);

    $result = AnimationCopier::copy($this->source, $this->build, 'ios');

    expect(file_get_contents("{$this->ios}/logo.lottie"))->toBe(file_get_contents($broken))
        ->and($result->warnings)->toHaveCount(1)
        ->and($result->warnings[0])->toContain('animations/logo.lottie');
});

it('gives the same result when run again', function (string $platform) {
    makeDotLottieV2("{$this->source}/logo.lottie", ['layers' => []]);
    file_put_contents("{$this->source}/wave.json", '{"layers":[]}');

    $first = AnimationCopier::copy($this->source, $this->build, $platform);
    $before = [zipEntries(destination($this->build, $platform, 'logo.lottie')), file_get_contents(destination($this->build, $platform, 'wave.json'))];

    $second = AnimationCopier::copy($this->source, $this->build, $platform);

    expect($second)->toEqual($first)
        ->and($second->warnings)->toBe([])
        ->and([zipEntries(destination($this->build, $platform, 'logo.lottie')), file_get_contents(destination($this->build, $platform, 'wave.json'))])->toBe($before);
})->with(['android', 'ios']);

it('quietly replaces the same animation another plugin copied', function () {
    $v2 = makeDotLottieV2("{$this->source}/splash.lottie", ['layers' => []]);
    DotLottie::toV1($v2, "{$this->ios}/splash.lottie");
    touch("{$this->ios}/splash.lottie", time() - 3600);

    expect(AnimationCopier::copy($this->source, $this->build, 'ios')->warnings)->toBe([]);
});

it('warns when it replaces a different animation another plugin copied', function () {
    makeDotLottieV1("{$this->source}/splash.lottie", ['layers' => []]);
    makeDotLottieV1("{$this->android}/splash.lottie", ['layers' => [['ty' => 4]]]);

    $result = AnimationCopier::copy($this->source, $this->build, 'android');

    expect(file_get_contents("{$this->android}/splash.lottie"))->toBe(file_get_contents("{$this->source}/splash.lottie"))
        ->and($result->warnings)->toHaveCount(1)
        ->and($result->warnings[0])->toContain('animations/splash.lottie');
});

it('warns about animations iOS would bundle under the same name', function () {
    makeDotLottieV1("{$this->source}/light/logo.lottie", ['layers' => []]);
    makeDotLottieV1("{$this->source}/dark/logo.lottie", ['layers' => []]);

    $ios = AnimationCopier::copy($this->source, $this->build, 'ios');
    $android = AnimationCopier::copy($this->source, $this->build, 'android');

    expect($ios->warnings)->toHaveCount(1)
        ->and($ios->warnings[0])->toContain('logo.lottie')
        ->and($android->warnings)->toBe([]);
});

it('copies nothing without a source directory or for another platform', function () {
    makeDotLottieV1("{$this->source}/logo.lottie", ['layers' => []]);

    expect(AnimationCopier::copy("{$this->source}/missing", $this->build, 'android')->copied)->toBe([])
        ->and(AnimationCopier::copy($this->source, $this->build, 'macos')->copied)->toBe([])
        ->and("{$this->build}/app")->not->toBeDirectory();
});

function destination(string $build, string $platform, string $file): string
{
    return $platform === 'ios'
        ? "{$build}/NativePHP/Resources/animations/{$file}"
        : "{$build}/app/src/main/assets/animations/{$file}";
}
