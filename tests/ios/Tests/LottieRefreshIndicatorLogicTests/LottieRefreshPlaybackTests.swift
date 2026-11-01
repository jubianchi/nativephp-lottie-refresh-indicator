import Testing
@testable import LottieRefreshIndicatorLogic

private let markers = LottieRefreshSegments.markers(pull: 0...0.5, refresh: 0.5...1)

struct LottieRefreshSegmentsTests {
    @Test func bothMarkersSplitTheAnimation() {
        #expect(markers == LottieRefreshSegments(pull: 0...0.5, refresh: 0.5...1))
    }

    @Test func withoutMarkersThePullIsTheWholeAnimationAndThereIsNoRefreshSegment() {
        #expect(LottieRefreshSegments.markers(pull: nil, refresh: nil) == LottieRefreshSegments(pull: 0...1, refresh: nil))
        #expect(LottieRefreshSegments.whole == LottieRefreshSegments(pull: 0...1, refresh: nil))
    }

    @Test func aPullMarkerAloneLeavesNoRefreshSegment() {
        #expect(LottieRefreshSegments.markers(pull: 0...0.5, refresh: nil) == LottieRefreshSegments(pull: 0...0.5, refresh: nil))
    }

    @Test func aRefreshMarkerAloneLeavesWhatComesBeforeItToThePull() {
        #expect(LottieRefreshSegments.markers(pull: nil, refresh: 0.5...1) == LottieRefreshSegments(pull: 0...0.5, refresh: 0.5...1))
    }

    @Test func aRefreshMarkerFromTheStartLeavesTheWholeAnimationToThePull() {
        #expect(LottieRefreshSegments.markers(pull: nil, refresh: 0...1) == LottieRefreshSegments(pull: 0...1, refresh: 0...1))
    }

    @Test func emptyMarkersAreIgnored() {
        #expect(LottieRefreshSegments.markers(pull: 0.2...0.2, refresh: 0.5...0.5) == LottieRefreshSegments(pull: 0...1, refresh: nil))
    }

    @Test func thePullMapsOntoThePullSegment() {
        #expect(markers.pullProgress(0) == 0)
        #expect(markers.pullProgress(0.5) == 0.25)
        #expect(markers.pullProgress(1) == 0.5)
        #expect(markers.pullProgress(1.4) == 0.5)
        #expect(markers.pullProgress(-0.3) == 0)
    }
}

struct LottieRefreshPlaybackTests {
    @Test func thePullDrivesTheAnimation() {
        var playback = LottieRefreshPlayback()

        #expect(playback.update(progress: 0.4, isRefreshing: false) == .show(0.4))
    }

    @Test func thePullIsClamped() {
        var playback = LottieRefreshPlayback()

        #expect(playback.update(progress: -0.2, isRefreshing: false) == .show(0))
        #expect(playback.update(progress: 1.6, isRefreshing: false) == .show(1))
    }

    @Test func thePullPlaysThePullSegment() {
        var playback = LottieRefreshPlayback()

        #expect(playback.update(progress: 0.5, isRefreshing: false, segments: markers) == .show(0.25))
        #expect(playback.update(progress: 1, isRefreshing: false, segments: markers) == .show(0.5))
    }

    @Test func aFullPullLoopsTheRefreshSegmentForward() {
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 1, isRefreshing: false, segments: markers)

        #expect(playback.update(progress: 1, isRefreshing: true, segments: markers) == .loop([
            LottieRefreshSegment(from: 0.5, to: 1, mode: .loop),
        ]))
    }

    @Test func aPartialPullFinishesThePullSegmentFirst() {
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 0.9, isRefreshing: false, segments: markers)

        #expect(playback.update(progress: 1, isRefreshing: true, segments: markers) == .loop([
            LottieRefreshSegment(from: 0.45, to: 0.5, mode: .once),
            LottieRefreshSegment(from: 0.5, to: 1, mode: .loop),
        ]))
    }

    @Test func withoutARefreshSegmentAFullPullPlaysThePullSegmentBackAndForth() {
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 1, isRefreshing: false)

        #expect(playback.update(progress: 1, isRefreshing: true) == .loop([
            LottieRefreshSegment(from: 1, to: 0, mode: .autoReverse),
        ]))
    }

    @Test func withoutARefreshSegmentAPartialPullCarriesOnToTheEndOfThePullSegment() {
        let pullOnly = LottieRefreshSegments.markers(pull: 0...0.5, refresh: nil)
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 0.6, isRefreshing: false, segments: pullOnly)

        #expect(playback.update(progress: 1, isRefreshing: true, segments: pullOnly) == .loop([
            LottieRefreshSegment(from: 0.3, to: 0.5, mode: .once),
            LottieRefreshSegment(from: 0.5, to: 0, mode: .autoReverse),
        ]))
    }

    @Test func theLoopStartsOnce() {
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 1, isRefreshing: false, segments: markers)
        _ = playback.update(progress: 1, isRefreshing: true, segments: markers)

        #expect(playback.update(progress: 1, isRefreshing: true, segments: markers) == .keep)
    }

    @Test func aRefreshShownWithoutAPullLoopsFromTheEndOfThePull() {
        var playback = LottieRefreshPlayback()

        #expect(playback.update(progress: 1, isRefreshing: true, segments: markers) == .loop([
            LottieRefreshSegment(from: 0.5, to: 1, mode: .loop),
        ]))
    }

    @Test func thePullTakesOverWhenTheRefreshEnds() {
        var playback = LottieRefreshPlayback()
        _ = playback.update(progress: 1, isRefreshing: false, segments: markers)
        _ = playback.update(progress: 1, isRefreshing: true, segments: markers)

        #expect(playback.update(progress: 0.6, isRefreshing: false, segments: markers) == .show(0.3))
    }

    @Test(arguments: [0, 0.3, 0.9, 0.999, 1], [
        LottieRefreshSegments.markers(pull: 0...0.5, refresh: 0.5...1),
        LottieRefreshSegments.markers(pull: nil, refresh: 0.5...1),
        LottieRefreshSegments.markers(pull: 0...0.5, refresh: nil),
        LottieRefreshSegments.markers(pull: nil, refresh: nil),
    ])
    func theAnimationNeverJumps(pull: Double, segments: LottieRefreshSegments) throws {
        var playback = LottieRefreshPlayback()
        guard case .show(let released) = playback.update(progress: pull, isRefreshing: false, segments: segments),
              case .loop(let plan) = playback.update(progress: 1, isRefreshing: true, segments: segments) else {
            Issue.record("the refresh did not start a loop")
            return
        }

        let frames = [released] + play(plan, frames: 400)

        var seams = 0
        for (index, (a, b)) in zip(frames, frames.dropFirst()).enumerated() {
            if let refresh = segments.refresh, abs(a - refresh.upperBound) < 0.02, abs(b - refresh.lowerBound) < 0.02 {
                seams += 1
            } else {
                #expect(abs(b - a) < 0.02, "jumped from \(a) to \(b) at frame \(index)")
                if segments.refresh != nil {
                    #expect(b >= a, "went back from \(a) to \(b) at frame \(index)")
                }
            }
        }
        if segments.refresh != nil {
            #expect(seams >= 3, "looped \(seams) times")
        }
    }
}

/// What a LottieAnimationView shows, frame after frame, playing the plan
/// at a constant speed: once from one end to the other, in a loop that
/// restarts at its start, or back and forth.
private func play(_ plan: [LottieRefreshSegment], frames: Int, step: Double = 0.01) -> [Double] {
    var shown: [Double] = []
    for segment in plan {
        let direction: Double = segment.to >= segment.from ? 1 : -1
        var position = segment.from
        var forward = true
        while shown.count < frames {
            let target = forward ? segment.to : segment.from
            let next = position + (forward ? direction : -direction) * step
            if (forward ? direction : -direction) * (next - target) >= 0 {
                position = target
                shown.append(position)
                switch segment.mode {
                case .once: break
                case .loop: position = segment.from; shown.append(position); continue
                case .autoReverse: forward.toggle(); continue
                }
                break
            }
            position = next
            shown.append(position)
        }
    }
    return Array(shown.prefix(frames))
}
