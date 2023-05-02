package com.android.samples.oNbeat


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.hamcrest.core.IsNot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4ClassRunner::class)
class GalleryFragmentTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

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
        textView.check(matches(isDisplayed()))

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
        onView(withId(R.id.image_number)).check(matches(IsNot.not(isDisplayed())))
        onView(withId(R.id.clear_selection)).check(matches(IsNot.not(isDisplayed())))

        val appCompatImageView3 = onView(
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
        appCompatImageView3.perform(click())

        val linearLayout = onView(
            allOf(
                withId(R.id.startLayout),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        2
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        linearLayout.perform(click())

        val linearLayout2 = onView(
            allOf(
                withId(R.id.sByImage),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.ImageOrDate),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        linearLayout2.perform(click())

        val appCompatImageView4 = onView(
            allOf(
                withId(R.id.image),
                childAtPosition(
                    allOf(
                        withId(R.id.imageLayout),
                        childAtPosition(
                            withId(R.id.gallery),
                            1
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatImageView4.perform(click())

        onView(withId(R.id.startDate)).check(matches(withText(`is`(not(equalTo(""))))))

        val linearLayout3 = onView(
            allOf(
                withId(R.id.stopLayout),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.LinearLayout")),
                        2
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        linearLayout3.perform(click())

        val linearLayout4 = onView(
            allOf(
                withId(R.id.sByDate),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.ImageOrDate),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        linearLayout4.perform(click())

        val materialButton = onView(
            allOf(
                withId(android.R.id.button1), withText("OK"),
                childAtPosition(
                    childAtPosition(
                        withClassName(`is`("android.widget.ScrollView")),
                        0
                    ),
                    3
                )
            )
        )
        materialButton.perform(scrollTo(), click())

        onView(withId(R.id.stopDate)).check(matches(withText(`is`(not(equalTo(""))))))

        val button = onView(
            allOf(
                withId(R.id.fab),
                withText("APPLY"),
                withContentDescription("Floating Action Button"),
                withParent(withParent(withId(R.id.gallery_fragment))),
                isDisplayed()
            )
        )
        button.check(matches(isDisplayed()))
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
