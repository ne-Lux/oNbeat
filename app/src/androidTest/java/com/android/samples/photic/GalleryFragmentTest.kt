package com.android.samples.photic


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class GalleryFragmentTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
        GrantPermissionRule.grant(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
        )

    @Test
    fun galleryFragmentTest() {
        val appCompatImageView = onView(
            allOf(
                withId(R.id.image),
                childAtPosition(
                    allOf(
                        withId(R.id.imageLayout),
                        childAtPosition(
                            withId(R.id.gallery),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageView.perform(click())

        val imageView = onView(
            allOf(
                withId(R.id.clear_selection),
                withParent(withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java))),
                isDisplayed()
            )
        )
        imageView.check(matches(isDisplayed()))

        val textView = onView(
            allOf(
                withId(R.id.image_number), withText("1"),
                withParent(withParent(IsInstanceOf.instanceOf(android.widget.LinearLayout::class.java))),
                isDisplayed()
            )
        )
        textView.check(matches(withText("1")))

        val appCompatImageView2 = onView(
            allOf(
                withId(R.id.clear_selection),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageView2.perform(click())
        onView(withId(R.id.image_number)).check(matches(not(isDisplayed())))
        onView(withId(R.id.clear_selection)).check(matches(not(isDisplayed())))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
