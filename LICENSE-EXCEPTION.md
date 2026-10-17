# Additional permission under GNU GPL version 3 section 7

Quasporium is licensed under the GNU General Public License version 3 (see [LICENSE](LICENSE)),
with the following additional permission.

If you modify this Program, or any covered work, by linking or combining it with
**7-Zip-JBinding-4Android** (or a modified version of that library), containing parts covered by
the terms of the GNU Lesser General Public License version 2.1 with the unRAR license restriction,
or with the **Google Firebase SDKs** or **Google Play services** libraries, the licensors of this
Program grant you additional permission to convey the resulting work. Corresponding Source for a
non-source form of such a combination shall include the source code for the parts of
7-Zip-JBinding-4Android used as well as that of the covered work.

## Why

- 7-Zip-JBinding-4Android unpacks downloaded game archives. Its native library contains the RAR
  decoder of 7-Zip, whose unRAR license restriction is not compatible with the GPL.
- The `play` build uses Firebase Crashlytics, Analytics and Performance Monitoring, which are
  proprietary. The `foss` build contains no Firebase or Google Play services code.

Without this permission the GPL would not allow distributing Quasporium together with these
libraries. The permission covers only them; everything else is under the plain GPL-3.0.
